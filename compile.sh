targetdir=target

if [ ! -d "$targetdir" ]; then mkdir $targetdir; fi

javac -sourcepath src -d $targetdir -cp lib/DTNConsoleConnection.jar:lib/ECLA.jar:lib/gson-2.8.6.jar:lib/hamcrest-core-1.3.jar:lib/junit-4.13.jar:lib/snakeyaml-1.26.jar src/core/*.java src/movement/*.java src/report/*.java src/routing/*.java src/gui/*.java src/input/*.java src/applications/*.java src/interfaces/*.java src/padec/**/*.java src/padec/rule/operator/rbac/*.java src/padec/rule/operator/*.java src/padec/filtering/techniques/*.java

if [ ! -d "$targetdir/gui/buttonGraphics" ]; then cp -R src/gui/buttonGraphics target/gui/; fi
	
