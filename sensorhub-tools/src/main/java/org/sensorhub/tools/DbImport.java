/***************************** BEGIN LICENSE BLOCK ***************************

 The contents of this file are Copyright (C) 2014 Sensia Software LLC.
 All Rights Reserved.

 Contributor(s): 
 Alexandre Robin <alex.robin@sensiasoftware.com>
 ******************************* END LICENSE BLOCK ***************************/

package org.sensorhub.tools;

import net.opengis.sensorml.v20.AbstractProcess;
import net.opengis.swe.v20.DataBlock;
import net.opengis.swe.v20.DataComponent;
import net.opengis.swe.v20.DataEncoding;
import net.opengis.swe.v20.TextEncoding;
import org.sensorhub.api.common.BigId;
import org.sensorhub.api.data.DataStreamInfo;
import org.sensorhub.api.data.ObsData;
import org.sensorhub.api.system.SystemId;
import org.sensorhub.impl.datastore.h2.MVObsSystemDatabase;
import org.sensorhub.impl.datastore.h2.MVObsSystemDatabaseConfig;
import org.sensorhub.impl.system.wrapper.SystemWrapper;
import org.sensorhub.utils.SWEDataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.cdm.common.DataStreamParser;
import org.vast.sensorML.SMLUtils;
import org.vast.swe.SWEHelper;
import org.vast.swe.SWEUtils;
import org.vast.swe.ScalarIndexer;
import org.vast.xml.DOMHelper;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;


public class DbImport
{
    private static final Logger log = LoggerFactory.getLogger(DbImport.class);


    private DbImport()
    {
    }


    public static void main(String[] args) throws Exception
    {
        if (args.length < 2)
        {
            System.out.println("Usage: DbImport export_file storage_path");
            System.exit(1);
        }

        // open storage
        String dbPath = args[1];
        Files.deleteIfExists(Path.of(dbPath));
        MVObsSystemDatabaseConfig mvDatabaseConfig = new MVObsSystemDatabaseConfig();
        mvDatabaseConfig.autoStart = true;
        mvDatabaseConfig.databaseNum = 1;
        mvDatabaseConfig.storagePath = dbPath;
        mvDatabaseConfig.memoryCacheSize = 1024;
        mvDatabaseConfig.autoCommitBufferSize = 1024;
        mvDatabaseConfig.autoCommitPeriod = 100;
        MVObsSystemDatabase db = new MVObsSystemDatabase();
        db.init(mvDatabaseConfig);
        db.start();

        // read XML metadata file
        File metadataFile = new File(args[0]);
        if (!metadataFile.exists()) {
            System.err.println("Missing DB export file: " + metadataFile);
            System.exit(1);
        }

        URL url = metadataFile.toURI().toURL();
        DOMHelper dom = new DOMHelper(url.toString(), false);
        SMLUtils smlUtils = new SMLUtils(SMLUtils.V2_0);
        SWEUtils sweUtils = new SWEUtils(SWEUtils.V2_0);

        // import each sensorML description
        String sysName = null;
        SystemId sysId = null;
        NodeList smlElts = dom.getElements(DbConstants.SECTION_SENSORML);
        for (int i = 0; i < smlElts.getLength(); i++)
        {
            Element processElt = dom.getFirstChildElement((Element) smlElts.item(i));
            AbstractProcess process = smlUtils.readProcess(dom, processElt);
            var fk = db.getSystemDescStore().add(new SystemWrapper(process));
            db.commit();
            sysId = new SystemId(fk.getInternalID(), process.getUniqueIdentifier());
            sysName = process.getName();
            System.out.println("Imported SensorML description of " + sysName);
        }

        // import data stores
        int metadataFileSuffixIdx = metadataFile.getAbsolutePath().lastIndexOf(".export.metadata");
        String dataPathPrefix = metadataFile.getAbsolutePath().substring(0, metadataFileSuffixIdx);
        NodeList dataStoreElts = dom.getElements(DbConstants.SECTION_DATASTORE);
        for (int i = 0; i < dataStoreElts.getLength(); i++)
        {
            Element dataStoreElt = (Element) dataStoreElts.item(i);
            String recordType = dom.getAttributeValue(dataStoreElt, "name");
            Element resultStructElt = dom.getElement(dataStoreElt, "elementType/*");
            DataComponent recordStruct = sweUtils.readComponent(dom, resultStructElt);
            Element resultEncodingElt = dom.getElement(dataStoreElt, "encoding/*");
            DataEncoding recordEncoding = sweUtils.readEncoding(dom, resultEncodingElt);

            recordStruct.setName(recordType);
            DataStreamInfo dsInfo = new DataStreamInfo.Builder()
                    .withName(sysName + " - " + recordType)
                    .withSystem(sysId)
                    .withRecordDescription(recordStruct)
                    .withRecordEncoding(recordEncoding)
                    .build();
            var dsKey = db.getObservationStore().getDataStreams().add(dsInfo);
            db.commit();
            System.out.println("Imported metadata for data store " + recordType);
            System.out.println("Importing records...");

            // read records data
            DataStreamParser recordParser = null;
            File dataFile = new File(dataPathPrefix + "." + recordType + ".export.data");
            if (!dataFile.exists())
            {
                System.err.println("Missing DB export file: " + dataFile);
                System.exit(1);
            }

            try (InputStream recordInput = new BufferedInputStream(new FileInputStream(dataFile)))
            {
                DataInputStream dis = new DataInputStream(recordInput);

                // prepare record writer
                recordParser = SWEHelper.createDataParser(recordEncoding);
                recordParser.setDataComponents(recordStruct);
                recordParser.setInput(recordInput);

                // write all records
                double prevTimeStamp = 0;
                int recordCount = 0;
                while (true)
                {
                    try
                    {
                        double timeStamp = dis.readDouble();
                        String producerID = dis.readUTF();
                        if (producerID.equals(DbConstants.KEY_NULL_PRODUCER))
                            producerID = null;

                        /*DataKey key = new DataKey(recordType, timeStamp);
                        key.producerID = producerID;
                        DataBlock dataBlk = recordParser.parseNextBlock();
                        db.storeRecord(key, dataBlk);*/

                        DataBlock dataBlk = recordParser.parseNextBlock();
                        if (recordEncoding instanceof TextEncoding)
                            dis.read(); // read '\n'

                        //System.out.println(timeStamp);
                        /*if (timeStamp == 1.616527476237289E9)
                            continue;
                        if (timeStamp - prevTimeStamp < 0.02)
                            continue;
                        prevTimeStamp = timeStamp;*/

                        var obs = new ObsData.Builder()
                                .withDataStream(dsKey.getInternalID())
                                .withPhenomenonTime(Instant.ofEpochMilli((long)(timeStamp*1000)))
                                .withResult(dataBlk)
                                .build();
                        db.getObservationStore().add(obs);

                        recordCount++;
                        if (recordCount % 100 == 0)
                        {
                            System.out.print(recordCount + "\r");
                            db.commit();
                        }
                    }
                    catch (EOFException e)
                    {
                        log.trace("No more records", e);
                        break;
                    }
                }

                System.out.println("Imported " + recordCount + " records");
            }
            finally
            {
                if (recordParser != null)
                    recordParser.close();
                db.commit();
            }
        }
    }
}
