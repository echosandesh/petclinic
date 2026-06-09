package org.springframework.samples.petclinic;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.samples.petclinic.owner.Owner;
import org.springframework.samples.petclinic.owner.OwnerRepository;
import org.springframework.samples.petclinic.owner.Pet;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class CustomPetClinicIntegrationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private OwnerRepository owners;

	@Autowired
	private RestTemplateBuilder builder;

	private RestTemplate getNonRedirectingRestTemplate() {
		return builder.rootUri("http://localhost:" + port).requestFactory(() -> new SimpleClientHttpRequestFactory() {
			@Override
			protected void prepareConnection(HttpURLConnection connection, String httpMethod) throws IOException {
				super.prepareConnection(connection, httpMethod);
				connection.setInstanceFollowRedirects(false);
			}
		}).build();
	}

	@Test
	void testCreateOwnerSuccess() {
		RestTemplate template = getNonRedirectingRestTemplate();

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("firstName", "John");
		map.add("lastName", "Doe");
		map.add("address", "123 Main Street");
		map.add("city", "Springfield");
		map.add("telephone", "1234567890");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
		ResponseEntity<String> response = template.postForEntity("/owners/new", request, String.class);

		// Expect redirect to the details page of the newly created owner
		assertThat(response.getStatusCode().is3xxRedirection()).isTrue();
		String location = response.getHeaders().getLocation().getPath();
		assertThat(location).startsWith("/owners/");

		// Retrieve from repo and check values
		String ownerIdStr = location.substring(location.lastIndexOf('/') + 1);
		if (ownerIdStr.contains(";")) {
			ownerIdStr = ownerIdStr.substring(0, ownerIdStr.indexOf(';'));
		}
		int ownerId = Integer.parseInt(ownerIdStr);
		Optional<Owner> ownerOpt = owners.findById(ownerId);
		assertThat(ownerOpt).isPresent();
		assertThat(ownerOpt.get().getFirstName()).isEqualTo("John");
		assertThat(ownerOpt.get().getLastName()).isEqualTo("Doe");
		assertThat(ownerOpt.get().getTelephone()).isEqualTo("1234567890");
	}

	@Test
	void testCreateOwnerValidationErrors() {
		RestTemplate template = getNonRedirectingRestTemplate();

		// Leave address and telephone empty (which are @NotBlank)
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("firstName", "John");
		map.add("lastName", "Doe");
		map.add("address", "");
		map.add("city", "Springfield");
		map.add("telephone", "");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
		ResponseEntity<String> response = template.postForEntity("/owners/new", request, String.class);

		// Should render the form again with OK status
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).contains("must not be blank");
	}

	@Test
	void testEditOwnerSuccess() {
		RestTemplate template = getNonRedirectingRestTemplate();

		// Update owner 1's details
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("firstName", "George");
		map.add("lastName", "Franklin");
		map.add("address", "999 Updated Lane");
		map.add("city", "Madison");
		map.add("telephone", "6085551023");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
		ResponseEntity<String> response = template.postForEntity("/owners/1/edit", request, String.class);

		assertThat(response.getStatusCode().is3xxRedirection()).isTrue();

		// Verify change in DB
		Optional<Owner> ownerOpt = owners.findById(1);
		assertThat(ownerOpt).isPresent();
		assertThat(ownerOpt.get().getAddress()).isEqualTo("999 Updated Lane");
	}

	@Test
	void testAddNewPetSuccess() {
		RestTemplate template = getNonRedirectingRestTemplate();

		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		map.add("name", "Buddy");
		map.add("birthDate", "2024-01-01");
		map.add("type", "dog");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
		ResponseEntity<String> response = template.postForEntity("/owners/1/pets/new", request, String.class);

		assertThat(response.getStatusCode().is3xxRedirection()).isTrue();

		// Verify pet added to owner 1
		Owner owner = owners.findById(1).orElseThrow();
		Pet pet = owner.getPet("Buddy");
		assertThat(pet).isNotNull();
		assertThat(pet.getType().getName()).isEqualTo("dog");
	}

	@Test
	void testShowResourcesVetListJson() {
		RestTemplate template = getNonRedirectingRestTemplate();

		ResponseEntity<String> response = template.getForEntity("/vets", String.class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		String body = response.getBody();
		assertThat(body).isNotNull();
		assertThat(body).contains("James");
		assertThat(body).contains("Helen");
	}

}
