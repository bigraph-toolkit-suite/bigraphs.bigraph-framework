---
id: bigrapher-converter
title: BigraphER
---


More information about BigraphER can be found on the website: [http://www.dcs.gla.ac.uk/~michele/bigrapher.html](http://www.dcs.gla.ac.uk/~michele/bigrapher.html)

## Exporting a `PureReactiveSystem`

```java
// specify some reactive system
PureReactiveSystem reactiveSystem = ...;

BigrapherTransformator prettyPrinter = new BigrapherTransformator();
String bigrapherEncoding = prettyPrinter.toString(reactiveSystem);
System.out.println(bigrapherEncoding);

FileOutputStream fout = new FileOutputStream(new File("bigrapher.big"));
prettyPrinter.toOutputStream(reactiveSystem, fout);
fout.close();
```

An exemplary output is shown below:

```text
ctrl Printer = 2;
ctrl Building = 0;
ctrl User = 1;
ctrl Room = 1;
ctrl Spool = 1;
ctrl Computer = 1;
ctrl Job = 0;
ctrl A = 1;
ctrl B = 1;

react parametricreactionrule0 = (Printer{a, b}.1 | Computer{b1}.Job.1 | User{jeff1}.1) || (Computer{b1}.( Job.1 | User{jeff2}.1 )) -> (Printer{a, b}.1 | Computer{b1}.Job.1 | User{jeff1}.1) || (Computer{b1}.( Job.1 | User{jeff2}.1 ));

big sample = Printer{a, b}.1 | Room{e0}.Computer{b1}.( Job.1 | User{jeff2}.1 ) | Room{e0}.( User{jeff1}.1 | Computer{b1}.Job.1 ) ;

big pred0 = Room{door}.( id(1) | User{name:jeff}.1 );
big pred1 = Room{door}.( User{name:jeff}.1 | id(1) );

begin brs
	init sample;
	rules = [{parametricreactionrule0}];
	preds = {pred0,pred1};
end
```