/** 
 * ============LICENSE_START=======================================================
 * org.onap.aai
 * ================================================================================
 * Copyright © 2017 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.onap.aai.edges;

import static org.junit.Assert.*;

import java.util.Collection;

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.onap.aai.edges.enums.AAIDirection;
import org.onap.aai.edges.enums.MultiplicityRule;
import org.onap.aai.edges.exceptions.AmbiguousRuleChoiceException;
import org.onap.aai.edges.exceptions.EdgeRuleNotFoundException;
import org.onap.aai.setup.SchemaLocationsBean;
import org.onap.aai.setup.Version;
import org.onap.aai.testutils.TestUtilConfigTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.collect.Multimap;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {SchemaLocationsBean.class, TestUtilConfigTranslator.class, EdgeIngestor.class})
@SpringBootTest
public class EdgeIngestorTest {
	@Autowired
	EdgeIngestor ei;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void getRulesTest1() throws EdgeRuleNotFoundException {
		EdgeRuleQuery q = new EdgeRuleQuery.Builder("foo").build();
		Multimap<String, EdgeRule> results = ei.getRules(q);
		assertTrue(results.size() == 5);
		assertTrue(results.containsKey("bar|foo"));
		
		assertTrue(2 == results.get("bar|foo").size());
		boolean seenLabel1 = false;
		boolean seenLabel2 = false;
		for(EdgeRule r : results.get("bar|foo")) {
			if ("eats".equals(r.getLabel())) {
				seenLabel1 = true;
			}
			if ("eatz".equals(r.getLabel())) {
				seenLabel2 = true;
			}
		}
		assertTrue(seenLabel1 && seenLabel2);
		
		assertTrue(results.containsKey("baz|foo"));
		assertTrue(results.containsKey("foo|quux"));
		assertTrue(results.containsKey("dog|foo"));
	}
	
	@Test
	public void getRulesTest2() throws EdgeRuleNotFoundException {
		EdgeRuleQuery q = new EdgeRuleQuery.Builder("puppy", "dog").build();
		Multimap<String, EdgeRule> results = ei.getRules(q);
		assertTrue(results.size() == 1);
		assertTrue(results.containsKey("dog|puppy"));
		Collection<EdgeRule> cr = results.get("dog|puppy");
		for (EdgeRule r : cr) {
			assertTrue("dog".equals(r.getFrom()));
			assertTrue("puppy".equals(r.getTo()));
			assertTrue("caresFor".equals(r.getLabel()));
			assertTrue(Direction.OUT.equals(r.getDirection()));
			assertTrue("One2Many".equalsIgnoreCase(r.getMultiplicityRule().toString()));
			assertTrue("NONE".equals(r.getContains()));
			assertTrue("OUT".equals(r.getDeleteOtherV()));
			assertTrue("NONE".equals(r.getPreventDelete()));
			assertTrue(r.isDefault());
		}
	}
	
	@Test
	public void getRulesTest3() throws EdgeRuleNotFoundException {
		EdgeRuleQuery q = new EdgeRuleQuery.Builder("l-interface").version(Version.V11).build();
		Multimap<String, EdgeRule> results = ei.getRules(q);
		assertTrue(results.size() == 4);
		assertTrue(results.containsKey("lag-interface|l-interface"));
		assertTrue(results.containsKey("l-interface|logical-link"));
		assertTrue(results.get("l-interface|logical-link").size() == 3);
	}
	
	@Test
	public void getRulesNoneFound() throws EdgeRuleNotFoundException {
		thrown.expect(EdgeRuleNotFoundException.class);
		thrown.expectMessage("No rules found for");
		EdgeRuleQuery q = new EdgeRuleQuery.Builder("l-interface").build();
		ei.getRules(q);
	}
	
	@Test
	public void getRuleSimpleTest() throws EdgeRuleNotFoundException, AmbiguousRuleChoiceException {
		EdgeRuleQuery q = new EdgeRuleQuery.Builder("notation", "parent").build();
		EdgeRule result = ei.getRule(q);
		assertTrue("parent".equals(result.getFrom()));
		assertTrue("notation".equals(result.getTo()));
		assertTrue("has".equals(result.getLabel()));
		assertTrue(Direction.OUT.equals(result.getDirection()));
		assertTrue(MultiplicityRule.MANY2MANY.equals(result.getMultiplicityRule()));
		assertTrue(AAIDirection.OUT.toString().equals(result.getContains()));
		assertTrue(AAIDirection.NONE.toString().equals(result.getDeleteOtherV()));
		assertTrue(AAIDirection.NONE.toString().equals(result.getPreventDelete()));
		assertTrue("parent contains notation".equals(result.getDescription()));
	}
	
	@Test
	public void getRuleWithDefaultTest() throws EdgeRuleNotFoundException, AmbiguousRuleChoiceException {
		EdgeRuleQuery q = new EdgeRuleQuery.Builder("l-interface","logical-link").version(Version.V11).build();	
		EdgeRule res = ei.getRule(q);
		assertTrue(res.isDefault());
		assertTrue("tosca.relationships.network.LinksTo".equals(res.getLabel()));
	}
	
	@Test
	public void getRuleWithNonDefault() throws EdgeRuleNotFoundException, AmbiguousRuleChoiceException {
		EdgeRuleQuery q = new EdgeRuleQuery.Builder("l-interface","logical-link").label("org.onap.relationships.inventory.Source").version(Version.V11).build();
		EdgeRule res = ei.getRule(q);
		assertFalse(res.isDefault());
		assertTrue("org.onap.relationships.inventory.Source".equals(res.getLabel()));
	}
	
	@Test
	public void getRuleNoneFoundTest() throws EdgeRuleNotFoundException, AmbiguousRuleChoiceException {
		thrown.expect(EdgeRuleNotFoundException.class);
		thrown.expectMessage("No rule found for");
		EdgeRuleQuery q = new EdgeRuleQuery.Builder("l-interface","nonexistent").build();
		ei.getRule(q);
	}
	
	@Test
	public void getRuleTooManyPairsTest() throws EdgeRuleNotFoundException, AmbiguousRuleChoiceException {
		thrown.expect(AmbiguousRuleChoiceException.class);
		thrown.expectMessage("No way to select single rule from these pairs:");
		EdgeRuleQuery q = new EdgeRuleQuery.Builder("foo").build();
		ei.getRule(q);
	}
	
	@Test
	public void getRuleAmbiguousDefaultTest() throws EdgeRuleNotFoundException, AmbiguousRuleChoiceException {
		thrown.expect(AmbiguousRuleChoiceException.class);
		thrown.expectMessage("Multiple defaults found.");
		EdgeRuleQuery q = new EdgeRuleQuery.Builder("seed","plant").version(Version.V11).build();
		ei.getRule(q);
	}
	
	@Test
	public void getRuleNoDefaultTest() throws EdgeRuleNotFoundException, AmbiguousRuleChoiceException {
		thrown.expect(AmbiguousRuleChoiceException.class);
		thrown.expectMessage("No default found.");
		EdgeRuleQuery q = new EdgeRuleQuery.Builder("apple", "orange").version(Version.V11).build();
		ei.getRule(q);
	}
	
	@Test
	public void hasRuleTest() {
		assertTrue(ei.hasRule(new EdgeRuleQuery.Builder("l-interface").version(Version.V11).build()));
		assertFalse(ei.hasRule(new EdgeRuleQuery.Builder("l-interface").build()));
	}
	
	@Test
	public void getCousinRulesTest() {
		Multimap<String, EdgeRule> results = ei.getCousinRules("dog");
		assertTrue(results.size() == 2);
		assertTrue(results.containsKey("dog|puppy"));
		assertTrue(results.containsKey("dog|foo"));
	}
	
	@Test
	public void getCousinRulesWithVersionTest() {
		Multimap<String, EdgeRule> results = ei.getCousinRules("foo", Version.V10);
		assertTrue(results.size() == 2);
		assertTrue(results.containsKey("bar|foo"));
		assertTrue(results.get("bar|foo").size() == 2);
	}
	
	@Test
	public void getCousinsNoneInVersionTest() {
		Multimap<String, EdgeRule> results = ei.getCousinRules("foo", Version.V11);
		assertTrue(results.isEmpty());
	}
	
	@Test
	public void hasCousinTest() {
		assertTrue(ei.hasCousinRule("foo"));
		assertTrue(ei.hasCousinRule("foo", Version.V10));
		assertFalse(ei.hasCousinRule("parent"));
		assertFalse(ei.hasCousinRule("foo", Version.V11));
	}

	@Test
	public void getChildRulesTest() {
		Multimap<String, EdgeRule> results = ei.getChildRules("parent");
		assertTrue(results.size() == 6);
		assertTrue(results.containsKey("notation|parent"));
		assertTrue(results.containsKey("not-notation|parent"));
		assertTrue(results.containsKey("out-out|parent"));
		assertTrue(results.containsKey("in-in|parent"));
		assertTrue(results.containsKey("in-out|parent"));
		assertTrue(results.containsKey("out-in|parent"));
	}
	
	@Test
	public void getChildRulesWithVersionTest() {
		Multimap<String, EdgeRule> results = ei.getChildRules("foo", Version.V10);
		assertTrue(results.size() == 2);
		assertTrue(results.containsKey("baz|foo"));
		assertTrue(results.containsKey("foo|quux"));
	}
	
	@Test
	public void getChildRulesNoneInVersionTest() {
		Multimap<String, EdgeRule> results = ei.getChildRules("foo", Version.V11);
		assertTrue(results.isEmpty());
	}
	
	@Test
	public void hasChildTest() {
		assertTrue(ei.hasChildRule("foo"));
		assertTrue(ei.hasChildRule("foo", Version.V10));
		assertFalse(ei.hasChildRule("puppy"));
		assertFalse(ei.hasChildRule("foo", Version.V11));
	}
	
	@Test
	public void getParentRulesTest() {
		Multimap<String, EdgeRule> results = ei.getParentRules("parent");
		assertTrue(results.size() == 6);
		assertTrue(results.containsKey("grandparent1|parent"));
		assertTrue(results.containsKey("grandparent2|parent"));
		assertTrue(results.containsKey("grandparent3|parent"));
		assertTrue(results.containsKey("grandparent4|parent"));
		assertTrue(results.containsKey("grandparent5|parent"));
		assertTrue(results.containsKey("grandparent6|parent"));
	}
	
	@Test
	public void getParentRulesWithVersionTest() {
		Multimap<String, EdgeRule> results = ei.getParentRules("baz", Version.V10);
		assertTrue(results.size() == 1);
		assertTrue(results.containsKey("baz|foo"));
	}
	
	@Test
	public void getParentRulesNoneInVersionTest() {
		Multimap<String, EdgeRule> results = ei.getParentRules("baz", Version.V11);
		assertTrue(results.isEmpty());
	}
	
	@Test
	public void hasParentTest() {
		assertTrue(ei.hasParentRule("parent"));
		assertTrue(ei.hasParentRule("quux", Version.V10));
		assertFalse(ei.hasParentRule("puppy"));
		assertFalse(ei.hasParentRule("foo", Version.V11));
	}
	
	@Test
	public void getAllCurrentRulesTest() throws EdgeRuleNotFoundException {
		Multimap<String, EdgeRule> res = ei.getAllCurrentRules();
		assertTrue(res.size() == 18);
	}
	
	@Test
	public void getAllRulesTest() throws EdgeRuleNotFoundException {
		Multimap<String, EdgeRule> res = ei.getAllRules(Version.V10);
		assertTrue(res.size() == 4);
		assertTrue(res.containsKey("bar|foo"));
		assertTrue(res.get("bar|foo").size() == 2);
		assertTrue(res.containsKey("baz|foo"));
		assertTrue(res.containsKey("foo|quux"));
		
		thrown.expect(EdgeRuleNotFoundException.class);
		thrown.expectMessage("No rules found for version V9.");
		ei.getAllRules(Version.V9);
	}
}
