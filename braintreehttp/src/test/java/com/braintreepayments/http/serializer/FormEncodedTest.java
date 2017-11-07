package com.braintreepayments.http.serializer;

import com.braintreepayments.http.HttpRequest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

public class FormEncodedTest {

	@Test
	public void testFormEncoded_encode_throwsWhenBodyNotMap() {
		HttpRequest request = new HttpRequest("/", "GET", Void.class);
		request.requestBody(new Object());

		FormEncoded formEncoded = new FormEncoded();

		try {
			formEncoded.encode(request);
			fail("Http client should have thrown for non-Map requestBody");
		} catch (IOException ioe) {
			assertEquals("Request requestBody must be Map<String, String> when Content-Type is application/x-www-form-urlencoded", ioe.getMessage());
		}
	}

	@Test
	public void testFormEncoded_encode_encodesData() throws IOException {
		HttpRequest<Void> request = new HttpRequest("/", "GET", Void.class);

		Map<String, String> data = new HashMap<>();
		data.put("key", "value");
		data.put("anotherkey", "anothervalue");

		request.requestBody(data);

		FormEncoded formEncoded = new FormEncoded();

		String encoded = new String(formEncoded.encode(request));

		// Order is non-deterministic
		String regex = "(key=value&anotherkey=anothervalue|anotherkey=anothervalue&key=value)";
		assertTrue(encoded.matches(regex));
	}

	@Test
	public void testFormEncoded_encode_escapesValues() throws IOException {
		HttpRequest<Void> request = new HttpRequest("/", "GET", Void.class);

		Map<String, String> data = new HashMap<>();
		data.put("key", "value with dashes and spaces");

		request.requestBody(data);

		FormEncoded formEncoded = new FormEncoded();

		String encoded = new String(formEncoded.encode(request));

		assertEquals("key=value%20with%20dashes%20and%20spaces", encoded);
	}

	@Test
	public void testFormEncoded_urlEscape() {
		String input = "some data !\"#$%&'()*+,-./";

		assertEquals("some%20data%20%21%22%23%24%25%26%27%28%29%2A%2B%2C%2D%2E%2F", FormEncoded.urlEscape(input));
	}
}
