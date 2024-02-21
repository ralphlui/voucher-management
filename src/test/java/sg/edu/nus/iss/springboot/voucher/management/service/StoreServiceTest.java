package sg.edu.nus.iss.springboot.voucher.management.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.TestPropertySource;

import sg.edu.nus.iss.springboot.voucher.management.entity.Store;
import sg.edu.nus.iss.springboot.voucher.management.repository.StoreRepository;
import sg.edu.nus.iss.springboot.voucher.management.service.impl.StoreService;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = { "DB_USERNAME=admin", "DB_PASSWORD=RDS_12345" })
public class StoreServiceTest {

	@Mock
	private StoreRepository storeRepo;

	@InjectMocks
	private StoreService storeService;

	Store testStore;

	@BeforeEach
	void setUp() {
		
		testStore = new Store("MUJI",
				"MUJI offers a wide variety of good quality items from stationery to household items and apparel.",
				"Test", "#04-36/40 Paragon Shopping Centre", "290 Orchard Rd", "", "238859", "Singapore", "Singapore",
				"Singapore", false);
	}

	@Test
	void getAllActiveStore() {
		when(storeRepo.findByIsDeletedFalse()).thenReturn(List.of(testStore));
		var storeList = storeService.findByIsDeletedFalse();
		assertThat(storeList).isNotNull();
		assertThat(storeList.size()).isEqualTo(1);
	}

	@Test
	void createStore() {

		String storeName = "";

		when(storeRepo.save(testStore)).thenReturn(testStore);

		Store createdStore = storeService.create(testStore);
		storeName = createdStore.getStoreName();

		assertThat(storeName).isNotNull();
		assertThat(storeName.equals("MUJI")).isTrue();
	}

}
