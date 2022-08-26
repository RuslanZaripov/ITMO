javac -cp @classpath -d compiled-files @sources
cd compiled-files
jar cf ../solution.jar ./info/kgeorgiy/ja/zaripov/implementor/*.class
pause