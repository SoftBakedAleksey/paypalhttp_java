package com.paypal.http.serializer;

import com.paypal.http.Zoo;
import com.paypal.http.annotations.ListOf;
import com.paypal.http.annotations.Model;
import com.paypal.http.annotations.SerializedName;
import com.paypal.http.exceptions.JsonParseException;
import com.paypal.http.exceptions.SerializeException;
import org.testng.annotations.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

public class JsonTest {

    @Test()
	public void testJson_serializesObjectsToJSON() throws SerializeException {
    	Zoo.Fins fishAppendages = new Zoo.Fins();
    	fishAppendages.dorsalFin = new Zoo.Appendage("back", 2);
    	fishAppendages.ventralFin = new Zoo.Appendage("front", 2);

        ArrayList<String> fishLocales = new ArrayList<>();
        fishLocales.add("ocean");
        fishLocales.add("lake");

        Zoo.Animal fish = new Zoo.Animal(
                "swimmy",
                3,
                10,
                fishAppendages,
                fishLocales,
                false
        );

        Zoo zoo = new Zoo(
                "Monterey Bay Aquarium",
                1,
                fish
        );

        String expected = "{\"name\":\"Monterey Bay Aquarium\",\"animal\":{\"locales\":[\"ocean\",\"lake\"],\"kind\":\"swimmy\",\"carnivorous\":false,\"weight\":10.0,\"age\":3,\"appendages\":{\"Dorsal fin\":{\"size\":2,\"location\":\"back\"},\"Ventral fin\":{\"size\":2,\"location\":\"front\"}}},\"number_of_animals\":1}";

        String s = new Json().serialize(zoo);
        assertEquals(s, expected);
    }

    @Test()
    public void testJson_serializesNestedMaps() throws SerializeException {
        HashMap<String, Object> map = new HashMap<>();

        HashMap<String, String> map1 = new HashMap<>();
        map1.put("key1", "value1");
        map1.put("key2", "value2");

        HashMap<String, String> map2 = new HashMap<>();
        map2.put("key3", "value3");
        map2.put("key4", "value4");

        map.put("map1", map1);
        map.put("map2", map2);

        String expected = "{\"map2\":{\"key3\":\"value3\",\"key4\":\"value4\"},\"map1\":{\"key1\":\"value1\",\"key2\":\"value2\"}}";

		String s = new Json().serialize(map);
		assertEquals(s, expected);
    }

    @Test()
    public void testJson_serializesArrays() throws SerializeException {

    	@Model
        class Base {
        	@SerializedName(value = "array", listClass = String.class)
            private List<String> array = new ArrayList<String>() {{ add("value1"); add("value2"); }};
        }

        String expected = "{\"array\":[\"value1\",\"value2\"]}";
        String actual = new Json().serialize(new Base());
        assertEquals(actual, expected);
    }

    /* Deserialize */

    @Test(expectedExceptions = JsonParseException.class)
    public void testJson_deserialize_errorsForNoOpenKeyQuote() throws IOException {
        String noStartQuote = "{\"my_key: \"value\"}";
		new Json().decode(noStartQuote, Map.class);
    }

	@Test(expectedExceptions = JsonParseException.class)
	public void testJson_deserialize_errorsForNoEndKeyQuote() throws IOException {
		String noStartQuote = "{my_key\": \"value\"}";
		new Json().decode(noStartQuote, Map.class);
	}

    @Test(expectedExceptions = JsonParseException.class)
    public void testJson_deserialize_throwsForJSONWithoutStartingBracket() throws IOException {
        String noStartBracket = "\"my_key\":\"my_value\"}";
		new Json().decode(noStartBracket, Map.class);
    }

	@Test(expectedExceptions = JsonParseException.class)
	public void testJson_deserialize_throwsForJSONWithoutEndingBracket() throws IOException {
		String noStartBracket = "{\"my_key\":\"my_value\"";
		new Json().decode(noStartBracket, Map.class);
	}

    @Test(expectedExceptions = JsonParseException.class)
    public void testJson_deserialize_throwsForJSONWithoutColon() throws IOException {
        String noColon = "{\"my_key\"\"my_value\"}";
        new Json().decode(noColon, Map.class);
    }

    @Test()
    public void testJson_deserialize_parsesScientificNotationCorrectly() throws IOException {
		String json = "{\"key\": 1.233e10}";

		Map<String, Double> deserialized = new Json().decode(json, Map.class);

		assertEquals(deserialized.get("key"), 1.233e10);
    }

    @Test()
    public void testJson_deserialize_parsesNull() throws IOException {
		String json = "{\"key\": null}";
		Map<String, Object> deserialized = new Json().decode(json, Map.class);

		assertNull(deserialized.get("key"));
    }

    @Test()
    public void testJson_deserialize_throwsForInvalidBoolean() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		String json = "{\"is_false\": faslee}";
    	Json j = new Json();

		try {
			Map<String, Object> a = j.decode(json, Map.class);
			fail("Expected IOException");
		} catch (IOException ite) {
			assertTrue(ite instanceof JsonParseException);
		}
    }

    @Test()
    public void testJson_deserialize_throwsForArrayMissingCommas() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    	Json j = new Json();
		String json = "{\"locales\":[\"ocean\" \"lake\"]}";

		try {
			Zoo.Animal a = j.decode(json, Zoo.Animal.class);
			fail("Expected IOException");
		} catch (IOException ite) {
			assertTrue(ite instanceof JsonParseException);
		}
    }

    @Test()
    public void testJson_deserialize_throwsForArrayMissingClose() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, IOException {
    	Json j = new Json();
    	String json = "{\"locales:[\"ocean\"}";

        try {
			Zoo.Animal a = j.decode(json, Zoo.Animal.class);
            fail("Expected IOException");
        } catch (IOException ite) {
        	assertTrue(ite instanceof JsonParseException);
        }
    }

    @Test
	public void testJson_deserialize_withWhiteSpace() throws IOException {
		String serializedZoo = "{\n" +
				"\t\"name\": \"Monterey Bay Aquarium\",\n" +
				"\t\"animal\": {\n" +
				"\t\t\"locales\": [\"ocean\", \"lake\"],\n" +
				"\t\t\"kind\": \"swimmy\",\n" +
				"\t\t\"carnivorous\": false,\n" +
				"\t\t\"weight\": 10,\n" +
				"\t\t\"age\": 3,\n" +
				"\t\t\"appendages\": {\n" +
				"\t\t\t\"Dorsal fin\": {\n" +
				"\t\t\t\t\"size\": 2,\n" +
				"\t\t\t\t\"location\": \"back\"\n" +
				"\t\t\t},\n" +
				"\t\t\t\"Ventral fin\": {\n" +
				"\t\t\t\t\"size\": 2,\n" +
				"\t\t\t\t\"location\": \"front\"\n" +
				"\t\t\t}\n" +
				"\t\t}\n" +
				"\t},\n" +
				"\t\"number_of_animals\": 1\n" +
				"}";

		Zoo zoo  = new Json().decode(serializedZoo, Zoo.class);

		assertEquals("Monterey Bay Aquarium", zoo.name);
		assertEquals(1, zoo.numberOfAnimals.intValue());
		assertEquals("swimmy", zoo.animal.kind);
		assertEquals(3, zoo.animal.age.intValue());
		assertEquals(10, zoo.animal.weight.intValue());
		assertEquals("back", zoo.animal.appendages.dorsalFin.location);
		assertEquals(2, zoo.animal.appendages.dorsalFin.size.intValue());
		assertEquals("front", zoo.animal.appendages.ventralFin.location);
		assertEquals(2, zoo.animal.appendages.ventralFin.size.intValue());
		assertEquals("ocean", zoo.animal.locales.get(0));
		assertEquals("lake", zoo.animal.locales.get(1));
		assertEquals(false, zoo.animal.carnivorous.booleanValue());
	}

    @Test()
    public void testJson_deserialize_createsAnObjectFromJSON() throws IOException {
       String serializedZoo = "{\"name\":\"Monterey Bay Aquarium\",\"animal\":{\"locales\":[\"ocean\",\"lake\"],\"kind\":\"swimmy\",\"carnivorous\":false,\"weight\":10,\"age\":3,\"appendages\":{\"Dorsal fin\":{\"size\":2,\"location\":\"back\"},\"Ventral fin\":{\"size\":2,\"location\":\"front\"}}},\"number_of_animals\":1}";

       Zoo zoo  = new Json().decode(serializedZoo, Zoo.class);

       assertEquals("Monterey Bay Aquarium", zoo.name);
       assertEquals(1, zoo.numberOfAnimals.intValue());
       assertEquals("swimmy", zoo.animal.kind);
       assertEquals(3, zoo.animal.age.intValue());
       assertEquals(10.0, zoo.animal.weight);
       assertEquals("back", zoo.animal.appendages.dorsalFin.location);
       assertEquals(2, zoo.animal.appendages.dorsalFin.size.intValue());
       assertEquals("front", zoo.animal.appendages.ventralFin.location);
       assertEquals(2, zoo.animal.appendages.ventralFin.size.intValue());
       assertEquals("ocean", zoo.animal.locales.get(0));
       assertEquals("lake", zoo.animal.locales.get(1));
       assertEquals(false, zoo.animal.carnivorous.booleanValue());
    }

	@Test()
	public void testJson_deserialize_createsAnObjectWithNullValues() throws IOException {
		String serializedZoo = "{\"name\":null, \"animal\":{\"locales\":[\"ocean\",\"lake\"]}}";

		Zoo zoo  = new Json().decode(serializedZoo, Zoo.class);

		assertEquals(null, zoo.name);
		assertEquals("ocean", zoo.animal.locales.get(0));
		assertEquals("lake", zoo.animal.locales.get(1));
	}

    @Test()
    public void testJson_createsAnObjectWithEscapeCharacter() throws IOException {
		ArrayList<String> fishLocales = new ArrayList<>();
		fishLocales.add("ocean");
		fishLocales.add("lake");

		Zoo.Animal fish = new Zoo.Animal(
				null,
				0,
				0,
				null,
				fishLocales,
				false
		);

		// create a object with input value of double quote and three back slash
		Zoo zoo = new Zoo(
				"test \\\", ",
				1,
				fish
		);

		String serializedZoo = new Json().serialize(zoo);
        zoo  = new Json().decode(serializedZoo, Zoo.class);

        assertEquals(zoo.name, "test \\\", ");
        assertEquals( zoo.animal.locales.get(0),"ocean");
        assertEquals(zoo.animal.locales.get(1),"lake");
    }

	@Model
	@ListOf(listClass = Zoo.class)
	public static class ZooList extends ArrayList<Zoo> {

		public ZooList() {
			super();
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testJson_deserialize_list() throws IOException {
		String montereyBay = "{\"name\":\"Monterey Bay Aquarium\",\"animal\":{\"locales\":[\"ocean\",\"lake\"],\"kind\":\"swimmy\",\"carnivorous\":false,\"weight\":10,\"age\":3,\"appendages\":{\"Dorsal fin\":{\"size\":2,\"location\":\"back\"},\"Ventral fin\":{\"size\":2,\"location\":\"front\"}}},\"number_of_animals\":1}";
		String shedd = "{\"name\":\"Shedd\",\"animal\":{\"locales\":[\"ocean\",\"lake\"],\"kind\":\"swimmy\",\"carnivorous\":true,\"weight\":10,\"age\":3,\"appendages\":{\"Dorsal fin\":{\"size\":2,\"location\":\"back\"},\"Ventral fin\":{\"size\":2,\"location\":\"front\"}}},\"number_of_animals\":1}";
		String serializedZoo = String.format("[%s, %s]", montereyBay, shedd);

		ZooList zoos = new Json().decode(serializedZoo, ZooList.class);

		assertEquals(zoos.size(), 2);

		Zoo monterey = zoos.get(0);

		assertEquals("Monterey Bay Aquarium", monterey.name);
		assertEquals(1, monterey.numberOfAnimals.intValue());
		assertEquals("swimmy", monterey.animal.kind);
		assertEquals(3, monterey.animal.age.intValue());
		assertEquals(10.0, monterey.animal.weight);
		assertEquals("back", monterey.animal.appendages.dorsalFin.location);
		assertEquals(2, monterey.animal.appendages.dorsalFin.size.intValue());
		assertEquals("front", monterey.animal.appendages.ventralFin.location);
		assertEquals(2, monterey.animal.appendages.ventralFin.size.intValue());
		assertEquals("ocean", monterey.animal.locales.get(0));
		assertEquals("lake", monterey.animal.locales.get(1));
		assertEquals(false, monterey.animal.carnivorous.booleanValue());

		Zoo sheddAq = zoos.get(1);
		assertEquals("Shedd", sheddAq.name);
		assertEquals(1, sheddAq.numberOfAnimals.intValue());
		assertEquals("swimmy", sheddAq.animal.kind);
		assertEquals(3, sheddAq.animal.age.intValue());
		assertEquals(10.0, sheddAq.animal.weight);
		assertEquals("back", sheddAq.animal.appendages.dorsalFin.location);
		assertEquals(2, sheddAq.animal.appendages.dorsalFin.size.intValue());
		assertEquals("front", sheddAq.animal.appendages.ventralFin.location);
		assertEquals(2, sheddAq.animal.appendages.ventralFin.size.intValue());
		assertEquals("ocean", sheddAq.animal.locales.get(0));
		assertEquals("lake", sheddAq.animal.locales.get(1));
		assertEquals(true, sheddAq.animal.carnivorous.booleanValue());
	}

    @Test
    public void testJson_deserialize_deserializesRawList() throws IOException {
		String serializedZoo = "[\"name\",\"Monterey Bay Aquarium\"]";

		List<String> strings = new Json().decode(serializedZoo, List.class);
		assertEquals(strings.size(), 2);
		assertEquals(strings.get(0), "name");
		assertEquals(strings.get(1), "Monterey Bay Aquarium");
	}

	@Test
	public void testJson_deserialize_deserializesEmptyObject() throws IOException {
    	String empty = "{}";
    	Map<String, Object> deserialized = new Json().decode(empty, Map.class);

    	assertEquals(deserialized.size(), 0);
	}

	@Test
	public void testJson_deserialize_deserializesEmptyList() throws IOException {
		String empty = "[]";
		List<Object> deserialized = new Json().decode(empty, List.class);

		assertEquals(deserialized.size(), 0);
	}

	@Test
	public void testJson_deserialize_deserializesWithCommasInValues() throws IOException {
		String empty = "{\"key\": \"one,two\", \"key_two\": \"four,five\"}";
		Map<String,Object> deserialized = new Json().decode(empty, Map.class);

		assertEquals(deserialized.size(), 2);
		assertEquals(deserialized.get("key"), "one,two");
		assertEquals(deserialized.get("key_two"), "four,five");
	}
	@Test
	public void testJson_deserialize_deserializeNestedList() throws IOException {
		String serializedZoo = "[[\"name\",\"Monterey Bay Aquarium\"],[\"Shedd\"]]";

		List<List<String>> strings = new Json().decode(serializedZoo, List.class);
		assertEquals(strings.size(), 2);
		assertEquals(strings.get(0).get(0), "name");
		assertEquals(strings.get(0).get(1), "Monterey Bay Aquarium");

		assertEquals(strings.get(1).get(0), "Shedd");
	}

	@Test
	public void testNestedEmptyObj() throws IOException {
		String empty = "{\"id\":{}}";

		Map<String, Object> obj = new Json().decode(empty, Map.class);
		assertNotNull(obj.get("id"));
	}

	@Test
	public void testNestedEmptyList() throws IOException {
		String empty = "{\"id\":[]}";

		Map<String,Object> obj = new Json().decode(empty, Map.class);
		assertNotNull(obj.get("id"));
		assertTrue(obj.get("id") instanceof List);
	}

	@Test
	public void testParsesEmptyString() throws IOException {
    	String json = "{\"name\": \"\"}";

    	Map<String, Object> deserialized = new Json().decode(json, Map.class);

    	assertEquals(deserialized.get("name"), "");
	}

	@Test
	public void testSquareBracketInsideCommas() throws IOException {
		String json = "{\"dd\": [{\"t\":{\"p\":{\"a\":\"Ozark Country Pic]\"}},\"cc\":\"US\"}]}";
		Map<String, Object> deserialized = new Json().decode(json, Map.class);
		assertNotNull(deserialized.get("dd"));
	}

	@Test
	public void testSquareBracketInsideCommasPlusEscapeComma() throws IOException {
		String json = "{\"dd\": [{\"t\":{\"p\":{\"a\":\"Ozark \\\"Country Pic]\"}},\"cc\":\"US\"}]}";
		Map<String, Object> deserialized = new Json().decode(json, Map.class);
		assertNotNull(deserialized.get("dd"));
	}

	@Test
	public void testSquareBracketInsideCommasHugeJSON() throws IOException {
		String json = "{\"transaction_details\":[{\"transaction_info\":{\"transaction_id\":\"0EX42053DT0482215\",\"transaction_event_code\":\"T1503\",\"transaction_initiation_date\":\"2022-03-06T19:47:04+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:04+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-1.66\"},\"transaction_status\":\"S\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387537.85\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387537.85\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"paypal_account_id\":\"MD6H5CZKYDYSG\",\"transaction_id\":\"184007338G1964002\",\"transaction_event_code\":\"T0001\",\"transaction_initiation_date\":\"2022-03-06T19:47:05+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:05+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-1.63\"},\"fee_amount\":{\"currency_code\":\"USD\",\"value\":\"-0.03\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387536.19\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387536.19\"},\"custom_field\":\"f145336e-c665-485c-96eb-1f6ca799357d\",\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"account_id\":\"MD6H5CZKYDYSG\",\"email_address\":\"ucoldiron@gmail.com\",\"address_status\":\"N\",\"payer_status\":\"Y\",\"payer_name\":{\"given_name\":\"utessa\",\"surname\":\"coldiron\",\"alternate_full_name\":\"utessa coldiron\"},\"country_code\":\"US\"},\"shipping_info\":{\"name\":\"Gil, Mincberg\"},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"15199121KB580914D\",\"paypal_reference_id\":\"184007338G1964002\",\"paypal_reference_id_type\":\"TXN\",\"transaction_event_code\":\"T1105\",\"transaction_initiation_date\":\"2022-03-06T19:47:05+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:05+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"1.66\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387537.85\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387537.85\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"2HK23156TB2224120\",\"transaction_event_code\":\"T1503\",\"transaction_initiation_date\":\"2022-03-06T19:47:09+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:09+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-2.32\"},\"transaction_status\":\"S\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387535.53\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387535.53\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"paypal_account_id\":\"F8SPS8NAC6W86\",\"transaction_id\":\"4GK183232Y609941J\",\"transaction_event_code\":\"T0001\",\"transaction_initiation_date\":\"2022-03-06T19:47:10+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:10+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-2.27\"},\"fee_amount\":{\"currency_code\":\"USD\",\"value\":\"-0.05\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387533.21\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387533.21\"},\"custom_field\":\"1e09e0fc-e40b-4a88-90d7-a59c4841a94f\",\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"account_id\":\"F8SPS8NAC6W86\",\"email_address\":\"josephwroblewski2018@gmail.com\",\"address_status\":\"N\",\"payer_status\":\"N\",\"payer_name\":{\"given_name\":\"Joseph\",\"surname\":\"Wroblewski\",\"alternate_full_name\":\"Joseph Wroblewski\"},\"country_code\":\"US\"},\"shipping_info\":{\"name\":\"Gil, Mincberg\"},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"5EP10745A08151029\",\"paypal_reference_id\":\"4GK183232Y609941J\",\"paypal_reference_id_type\":\"TXN\",\"transaction_event_code\":\"T1105\",\"transaction_initiation_date\":\"2022-03-06T19:47:10+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:10+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"2.32\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387535.53\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387535.53\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"4A304997VF4346235\",\"transaction_event_code\":\"T1503\",\"transaction_initiation_date\":\"2022-03-06T19:47:14+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:14+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-2.11\"},\"transaction_status\":\"S\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387533.42\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387533.42\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"paypal_account_id\":\"98JW5WNU5WN2N\",\"transaction_id\":\"7VJ69002M9085432H\",\"transaction_event_code\":\"T0001\",\"transaction_initiation_date\":\"2022-03-06T19:47:14+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:14+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-2.07\"},\"fee_amount\":{\"currency_code\":\"USD\",\"value\":\"-0.04\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387531.31\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387531.31\"},\"custom_field\":\"da5d3b72-3676-43ce-aa48-0202b4e89c2e\",\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"account_id\":\"98JW5WNU5WN2N\",\"email_address\":\"pamelarossi208@gmail.com\",\"address_status\":\"N\",\"payer_status\":\"N\",\"payer_name\":{\"alternate_full_name\":\"Pamela rossi 37\"},\"country_code\":\"US\"},\"shipping_info\":{\"name\":\"Gil, Mincberg\"},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"5AM64066P2768163F\",\"paypal_reference_id\":\"7VJ69002M9085432H\",\"paypal_reference_id_type\":\"TXN\",\"transaction_event_code\":\"T1105\",\"transaction_initiation_date\":\"2022-03-06T19:47:14+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:14+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"2.11\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387533.42\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387533.42\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"9L85093144444331D\",\"transaction_event_code\":\"T1503\",\"transaction_initiation_date\":\"2022-03-06T19:47:19+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:19+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-2.10\"},\"transaction_status\":\"S\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387531.32\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387531.32\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"4HE36094PE860193T\",\"transaction_event_code\":\"T1503\",\"transaction_initiation_date\":\"2022-03-06T19:47:19+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:19+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-2.23\"},\"transaction_status\":\"S\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387529.09\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387529.09\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"paypal_account_id\":\"842G7HAMYFJKE\",\"transaction_id\":\"5DM618570H083600U\",\"transaction_event_code\":\"T0001\",\"transaction_initiation_date\":\"2022-03-06T19:47:20+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:20+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-2.06\"},\"fee_amount\":{\"currency_code\":\"USD\",\"value\":\"-0.04\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387526.99\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387526.99\"},\"custom_field\":\"142da19e-2180-4bf1-81c0-45fdda651f90\",\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"account_id\":\"842G7HAMYFJKE\",\"email_address\":\"melissakingman52@gmail.com\",\"address_status\":\"N\",\"payer_status\":\"N\",\"payer_name\":{\"given_name\":\"Melissa\",\"surname\":\"Kingman\",\"alternate_full_name\":\"Melissa Kingman\"},\"country_code\":\"US\"},\"shipping_info\":{\"name\":\"Gil, Mincberg\"},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"6L874932V57812000\",\"paypal_reference_id\":\"5DM618570H083600U\",\"paypal_reference_id_type\":\"TXN\",\"transaction_event_code\":\"T1105\",\"transaction_initiation_date\":\"2022-03-06T19:47:20+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:20+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"2.10\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387529.09\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387529.09\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"paypal_account_id\":\"YQADV3H5CF9SQ\",\"transaction_id\":\"3PB41523CW0149226\",\"transaction_event_code\":\"T0001\",\"transaction_initiation_date\":\"2022-03-06T19:47:20+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:20+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-2.19\"},\"fee_amount\":{\"currency_code\":\"USD\",\"value\":\"-0.04\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387526.86\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387526.86\"},\"custom_field\":\"111dff17-f5ce-4fd2-b8af-eba4330b259e\",\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"account_id\":\"YQADV3H5CF9SQ\",\"email_address\":\"toniagraham402@gmail.com\",\"address_status\":\"N\",\"payer_status\":\"N\",\"payer_name\":{\"given_name\":\"Tonia\",\"surname\":\"Graham\",\"alternate_full_name\":\"Tonia Graham\"},\"country_code\":\"US\"},\"shipping_info\":{\"name\":\"Gil, Mincberg\"},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"7U961036A0935260X\",\"paypal_reference_id\":\"3PB41523CW0149226\",\"paypal_reference_id_type\":\"TXN\",\"transaction_event_code\":\"T1105\",\"transaction_initiation_date\":\"2022-03-06T19:47:20+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:20+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"2.23\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387529.09\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387529.09\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"95M35388M5438074N\",\"transaction_event_code\":\"T1503\",\"transaction_initiation_date\":\"2022-03-06T19:47:29+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:29+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-5.57\"},\"transaction_status\":\"S\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387523.52\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387523.52\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"19V86302FF0687208\",\"transaction_event_code\":\"T1503\",\"transaction_initiation_date\":\"2022-03-06T19:47:29+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:29+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-6.28\"},\"transaction_status\":\"S\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387517.24\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387517.24\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"paypal_account_id\":\"WFZ6EF6U74F42\",\"transaction_id\":\"0PS396326V436162M\",\"transaction_event_code\":\"T0001\",\"transaction_initiation_date\":\"2022-03-06T19:47:30+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:30+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-5.46\"},\"fee_amount\":{\"currency_code\":\"USD\",\"value\":\"-0.11\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387511.67\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387511.67\"},\"custom_field\":\"4b2b0574-6bd9-4460-8695-61ac619b0785\",\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"account_id\":\"WFZ6EF6U74F42\",\"email_address\":\"RetiredMxMan@gmail.com\",\"address_status\":\"N\",\"payer_status\":\"Y\",\"payer_name\":{\"given_name\":\"Ed\",\"surname\":\"Plants\",\"alternate_full_name\":\"Ed Plants\"},\"country_code\":\"US\"},\"shipping_info\":{\"name\":\"Gil, Mincberg\"},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"4BY03360DX689433C\",\"paypal_reference_id\":\"0PS396326V436162M\",\"paypal_reference_id_type\":\"TXN\",\"transaction_event_code\":\"T1105\",\"transaction_initiation_date\":\"2022-03-06T19:47:30+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:30+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"5.57\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387517.24\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387517.24\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"paypal_account_id\":\"8WRKC3UAX54Z4\",\"transaction_id\":\"6FG183111A138442E\",\"transaction_event_code\":\"T0001\",\"transaction_initiation_date\":\"2022-03-06T19:47:30+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:30+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-6.16\"},\"fee_amount\":{\"currency_code\":\"USD\",\"value\":\"-0.12\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387510.96\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387510.96\"},\"custom_field\":\"784b591c-84f5-4fce-80da-ee9b341298b9\",\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"account_id\":\"8WRKC3UAX54Z4\",\"email_address\":\"shaquonsmith524@gmail.com\",\"address_status\":\"N\",\"payer_status\":\"N\",\"payer_name\":{\"given_name\":\"Shaquon\",\"surname\":\"Smith\",\"alternate_full_name\":\"Shaquon Smith\"},\"country_code\":\"US\"},\"shipping_info\":{\"name\":\"Gil, Mincberg\"},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"2ED02624FT181273V\",\"paypal_reference_id\":\"6FG183111A138442E\",\"paypal_reference_id_type\":\"TXN\",\"transaction_event_code\":\"T1105\",\"transaction_initiation_date\":\"2022-03-06T19:47:30+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:30+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"6.28\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387517.24\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387517.24\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"3AB90891SS521711D\",\"transaction_event_code\":\"T1503\",\"transaction_initiation_date\":\"2022-03-06T19:47:40+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:40+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-2.28\"},\"transaction_status\":\"S\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387514.96\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387514.96\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"paypal_account_id\":\"E7CX3SYQN8TAU\",\"transaction_id\":\"54N50959BT958030H\",\"transaction_event_code\":\"T0001\",\"transaction_initiation_date\":\"2022-03-06T19:47:41+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:41+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-2.24\"},\"fee_amount\":{\"currency_code\":\"USD\",\"value\":\"-0.04\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387512.68\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387512.68\"},\"custom_field\":\"17652982-87f5-4c1c-af75-cd2457a71292\",\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"account_id\":\"E7CX3SYQN8TAU\",\"email_address\":\"sandra.house56@gmail.com\",\"address_status\":\"N\",\"payer_status\":\"N\",\"payer_name\":{\"given_name\":\"Sandra\",\"surname\":\"Myrick\",\"alternate_full_name\":\"Sandra Myrick\"},\"country_code\":\"US\"},\"shipping_info\":{\"name\":\"Gil, Mincberg\"},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"2SE012222T687081Y\",\"paypal_reference_id\":\"54N50959BT958030H\",\"paypal_reference_id_type\":\"TXN\",\"transaction_event_code\":\"T1105\",\"transaction_initiation_date\":\"2022-03-06T19:47:41+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:41+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"2.28\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387514.96\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387514.96\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"83U69498X41317044\",\"transaction_event_code\":\"T1503\",\"transaction_initiation_date\":\"2022-03-06T19:47:41+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:41+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-3.27\"},\"transaction_status\":\"S\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387511.69\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387511.69\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"paypal_account_id\":\"HGR6ZUPCQBA4G\",\"transaction_id\":\"76C852908B976671X\",\"transaction_event_code\":\"T0001\",\"transaction_initiation_date\":\"2022-03-06T19:47:42+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:42+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-3.21\"},\"fee_amount\":{\"currency_code\":\"USD\",\"value\":\"-0.06\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387508.42\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387508.42\"},\"custom_field\":\"371e74ba-3db6-4f09-8e7b-071649b875cc\",\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"account_id\":\"HGR6ZUPCQBA4G\",\"email_address\":\"Michaelmorocco@aol.com\",\"address_status\":\"N\",\"payer_status\":\"N\",\"payer_name\":{\"given_name\":\"Michael\",\"surname\":\"Morocco\",\"alternate_full_name\":\"Michael Morocco\"},\"country_code\":\"US\"},\"shipping_info\":{\"name\":\"Gil, Mincberg\"},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"7045327776055292U\",\"paypal_reference_id\":\"76C852908B976671X\",\"paypal_reference_id_type\":\"TXN\",\"transaction_event_code\":\"T1105\",\"transaction_initiation_date\":\"2022-03-06T19:47:42+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:42+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"3.27\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387511.69\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387511.69\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"5TU49517J4731024F\",\"transaction_event_code\":\"T1503\",\"transaction_initiation_date\":\"2022-03-06T19:47:42+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:42+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-6.94\"},\"transaction_status\":\"S\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387504.75\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387504.75\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"paypal_account_id\":\"HGR6ZUPCQBA4G\",\"transaction_id\":\"0SR246476M901010R\",\"transaction_event_code\":\"T0001\",\"transaction_initiation_date\":\"2022-03-06T19:47:43+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:43+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-6.80\"},\"fee_amount\":{\"currency_code\":\"USD\",\"value\":\"-0.14\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387497.81\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387497.81\"},\"custom_field\":\"26b2afb3-e897-419c-813e-932c0461a802\",\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"account_id\":\"HGR6ZUPCQBA4G\",\"email_address\":\"Michaelmorocco@aol.com\",\"address_status\":\"N\",\"payer_status\":\"N\",\"payer_name\":{\"given_name\":\"Michael\",\"surname\":\"Morocco\",\"alternate_full_name\":\"Michael Morocco\"},\"country_code\":\"US\"},\"shipping_info\":{\"name\":\"Gil, Mincberg\"},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"1JF564382K624805M\",\"paypal_reference_id\":\"0SR246476M901010R\",\"paypal_reference_id_type\":\"TXN\",\"transaction_event_code\":\"T1105\",\"transaction_initiation_date\":\"2022-03-06T19:47:43+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:43+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"6.94\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387504.75\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387504.75\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"253475244N277235B\",\"transaction_event_code\":\"T1503\",\"transaction_initiation_date\":\"2022-03-06T19:47:46+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:46+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-3.67\"},\"transaction_status\":\"S\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387501.08\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387501.08\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"paypal_account_id\":\"S9BWATNZWVXP6\",\"transaction_id\":\"2AY566254X083791S\",\"transaction_event_code\":\"T0001\",\"transaction_initiation_date\":\"2022-03-06T19:47:47+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:47+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-3.60\"},\"fee_amount\":{\"currency_code\":\"USD\",\"value\":\"-0.07\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387497.41\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387497.41\"},\"custom_field\":\"8709f29a-233f-43ff-96b2-08cd06454141\",\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"account_id\":\"S9BWATNZWVXP6\",\"email_address\":\"Ozarkcountrypickins@gmail.com\",\"address_status\":\"N\",\"payer_status\":\"N\",\"payer_name\":{\"alternate_full_name\":\"Ozark Country Pic]\"},\"country_code\":\"US\"},\"shipping_info\":{\"name\":\"Gil, Mincberg\"},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"79070438G1766892P\",\"paypal_reference_id\":\"2AY566254X083791S\",\"paypal_reference_id_type\":\"TXN\",\"transaction_event_code\":\"T1105\",\"transaction_initiation_date\":\"2022-03-06T19:47:47+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:47+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"3.67\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387501.08\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387501.08\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"5A7989482E987483E\",\"transaction_event_code\":\"T1503\",\"transaction_initiation_date\":\"2022-03-06T19:47:54+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:54+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-2.96\"},\"transaction_status\":\"S\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387498.12\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387498.12\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"paypal_account_id\":\"QPXXCUQFA7TWQ\",\"transaction_id\":\"9YF90784NF610432J\",\"transaction_event_code\":\"T0001\",\"transaction_initiation_date\":\"2022-03-06T19:47:54+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:54+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-2.90\"},\"fee_amount\":{\"currency_code\":\"USD\",\"value\":\"-0.06\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387495.16\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387495.16\"},\"custom_field\":\"659173c7-e92f-46b2-99c3-e8932c4ab1e3\",\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"account_id\":\"QPXXCUQFA7TWQ\",\"email_address\":\"deannasimons22@gmail.com\",\"address_status\":\"N\",\"payer_status\":\"N\",\"payer_name\":{\"given_name\":\"Deanna \",\"surname\":\"Simons\",\"alternate_full_name\":\"Deanna  Simons\"},\"country_code\":\"US\"},\"shipping_info\":{\"name\":\"Gil, Mincberg\"},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"73F028658W3514943\",\"paypal_reference_id\":\"9YF90784NF610432J\",\"paypal_reference_id_type\":\"TXN\",\"transaction_event_code\":\"T1105\",\"transaction_initiation_date\":\"2022-03-06T19:47:54+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:54+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"2.96\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387498.12\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387498.12\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"06H28604UD4032531\",\"transaction_event_code\":\"T1503\",\"transaction_initiation_date\":\"2022-03-06T19:47:56+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:56+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-2.98\"},\"transaction_status\":\"S\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387495.14\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387495.14\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"paypal_account_id\":\"QLEKPFYMCV2ZJ\",\"transaction_id\":\"74E70510MP990882T\",\"transaction_event_code\":\"T0001\",\"transaction_initiation_date\":\"2022-03-06T19:47:57+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:57+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-2.92\"},\"fee_amount\":{\"currency_code\":\"USD\",\"value\":\"-0.06\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387492.16\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387492.16\"},\"custom_field\":\"89c5b560-d730-481b-9102-699dbe7062f5\",\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"account_id\":\"QLEKPFYMCV2ZJ\",\"email_address\":\"jeffmgo@ymail.com\",\"address_status\":\"N\",\"payer_status\":\"N\",\"payer_name\":{\"alternate_full_name\":\"webdesign\"},\"country_code\":\"US\"},\"shipping_info\":{\"name\":\"Gil, Mincberg\"},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"1KT22308KH286431T\",\"paypal_reference_id\":\"74E70510MP990882T\",\"paypal_reference_id_type\":\"TXN\",\"transaction_event_code\":\"T1105\",\"transaction_initiation_date\":\"2022-03-06T19:47:57+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:57+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"2.98\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387495.14\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387495.14\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"44S12565NK0311314\",\"transaction_event_code\":\"T1503\",\"transaction_initiation_date\":\"2022-03-06T19:47:59+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:59+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-0.39\"},\"transaction_status\":\"S\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387494.75\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387494.75\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"240874871R790900F\",\"transaction_event_code\":\"T1503\",\"transaction_initiation_date\":\"2022-03-06T19:47:59+0000\",\"transaction_updated_date\":\"2022-03-06T19:47:59+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-2.03\"},\"transaction_status\":\"S\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387492.72\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387492.72\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"paypal_account_id\":\"M9LEEAD4ELEN2\",\"transaction_id\":\"34C27544JL739533B\",\"transaction_event_code\":\"T0001\",\"transaction_initiation_date\":\"2022-03-06T19:48:00+0000\",\"transaction_updated_date\":\"2022-03-06T19:48:00+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-0.38\"},\"fee_amount\":{\"currency_code\":\"USD\",\"value\":\"-0.01\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387492.33\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387492.33\"},\"custom_field\":\"d5de453c-b1fc-4d23-bafb-a1837b18cb03\",\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"account_id\":\"M9LEEAD4ELEN2\",\"email_address\":\"jerrywbrown44@gmail.com\",\"address_status\":\"N\",\"payer_status\":\"N\",\"payer_name\":{\"given_name\":\"Jerry\",\"surname\":\"Brown\",\"alternate_full_name\":\"Jerry Brown\"},\"country_code\":\"US\"},\"shipping_info\":{\"name\":\"Gil, Mincberg\"},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"7VH66571KJ2902529\",\"paypal_reference_id\":\"34C27544JL739533B\",\"paypal_reference_id_type\":\"TXN\",\"transaction_event_code\":\"T1105\",\"transaction_initiation_date\":\"2022-03-06T19:48:00+0000\",\"transaction_updated_date\":\"2022-03-06T19:48:00+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"0.39\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387492.72\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387492.72\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"paypal_account_id\":\"S6XMVE3E37PY2\",\"transaction_id\":\"9F209481HU186163R\",\"transaction_event_code\":\"T0001\",\"transaction_initiation_date\":\"2022-03-06T19:48:00+0000\",\"transaction_updated_date\":\"2022-03-06T19:48:00+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"-1.99\"},\"fee_amount\":{\"currency_code\":\"USD\",\"value\":\"-0.04\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387490.69\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387490.69\"},\"custom_field\":\"963ac075-b7c7-4c89-a2f7-28493e6f7f2c\",\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"account_id\":\"S6XMVE3E37PY2\",\"email_address\":\"lokismary1015@gmail.com\",\"address_status\":\"N\",\"payer_status\":\"Y\",\"payer_name\":{\"given_name\":\"Mary\",\"surname\":\"Hernandez\",\"alternate_full_name\":\"Mary Hernandez\"},\"country_code\":\"US\"},\"shipping_info\":{\"name\":\"Gil, Mincberg\"},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}},{\"transaction_info\":{\"transaction_id\":\"52874620E4876541V\",\"paypal_reference_id\":\"9F209481HU186163R\",\"paypal_reference_id_type\":\"TXN\",\"transaction_event_code\":\"T1105\",\"transaction_initiation_date\":\"2022-03-06T19:48:00+0000\",\"transaction_updated_date\":\"2022-03-06T19:48:00+0000\",\"transaction_amount\":{\"currency_code\":\"USD\",\"value\":\"2.03\"},\"transaction_status\":\"S\",\"transaction_subject\":\"Your reward from JustPlay!\",\"ending_balance\":{\"currency_code\":\"USD\",\"value\":\"387492.72\"},\"available_balance\":{\"currency_code\":\"USD\",\"value\":\"387492.72\"},\"protection_eligibility\":\"02\",\"instrument_type\":\"PAYPAL\"},\"payer_info\":{\"address_status\":\"N\",\"payer_name\":{}},\"shipping_info\":{},\"cart_info\":{},\"store_info\":{},\"auction_info\":{},\"incentive_info\":{}}],\"account_number\":\"L7ZW8D3VQ6TKC\",\"start_date\":\"2022-03-06T19:47:00+0000\",\"end_date\":\"2022-03-06T19:48:00+0000\",\"last_refreshed_datetime\":\"2022-03-08T05:29:59+0000\",\"page\":1,\"total_items\":45,\"total_pages\":1,\"links\":[{\"href\":\"https://api.paypal.com/v1/reporting/transactions?end_date=2022-03-06T19%3A48%3A00-0000&fields=all&start_date=2022-03-06T19%3A47%3A00-0000&page_size=100&page=1\",\"rel\":\"self\",\"method\":\"GET\"}]}";
		Map<String, Object> deserialized = new Json().decode(json, Map.class);
		assertNotNull(deserialized.get("transaction_details"));
	}
}
