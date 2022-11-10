setlocal enabledelayedexpansion
for /f %%a IN ('dir /b /s %~1"\*.dat.export.metadata"') do (
        set input=%%a
        set fname=%%a
        set output=!fname:~0,-20%!_V2.dat
        call java -cp "./lib/*" org.sensorhub.tools.DbImport !input! !output!
    )
endlocal