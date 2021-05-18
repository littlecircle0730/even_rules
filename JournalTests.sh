./compile.sh
for file in journal_settings/*; do
java -Xmx512M -cp target:lib/ECLA.jar:lib/DTNConsoleConnection.jar:lib/gson-2.8.6.jar:lib/snakeyaml-1.26.jar core.DTNSim -b 1 $file
done