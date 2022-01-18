
public void bigrapher_test01() throws InvalidConnectionException, TypeNotExistsException, InvalidReactionRuleException, IOException { 
	// ...
	PureReactiveSystem rs = new PureReactiveSystem();
	rs.setAgent(s0);
	rs.addReactionRule(snd);
	rs.addReactionRule(ready);
	rs.addReactionRule(lambda);
	rs.addReactionRule(newRR);
	rs.addPredicate(phi);
	BigrapherTransformator transformator = new BigrapherTransformator();
	transformator.toOutputStream(rs, System.out);
}
