package sg.edu.nus.iss.springboot.voucher.management.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import sg.edu.nus.iss.springboot.voucher.management.dto.CampaignDTO;
import sg.edu.nus.iss.springboot.voucher.management.entity.Campaign;
import sg.edu.nus.iss.springboot.voucher.management.entity.Store;
import sg.edu.nus.iss.springboot.voucher.management.entity.User;
import sg.edu.nus.iss.springboot.voucher.management.enums.CampaignStatus;
import sg.edu.nus.iss.springboot.voucher.management.enums.RoleType;
import sg.edu.nus.iss.springboot.voucher.management.repository.CampaignRepository;
import sg.edu.nus.iss.springboot.voucher.management.repository.StoreRepository;
import sg.edu.nus.iss.springboot.voucher.management.repository.UserRepository;
import sg.edu.nus.iss.springboot.voucher.management.service.impl.CampaignService;

@SpringBootTest
public class CampaignServiceTest {
    
    @Mock
    private CampaignRepository campaignRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private CampaignService campaignService;

    private static List<Campaign> mockCampaigns = new ArrayList<>();
    private static User user = new User("1","test@email.com", "username", "pwd", RoleType.CUSTOMER, null, null, true, null, null, null, null, null, null);
    private static Store store = new Store("1", "Store name 1", "description", null, null, null, null, null, null, null, null, null, null, null, user, null, user, false, null);
    private static Campaign campaign1 = new Campaign("1", "new campaign 1", store, CampaignStatus.CREATED, null, 0, 0, null, null, null, user, user, null, null, null);
    private static Campaign campaign2 = new Campaign("2", "new campaign 2", store, CampaignStatus.CREATED, null, 0, 0, null, null, null, user, user, null, null, null);
   
    @BeforeAll
    static void setUp(){
        mockCampaigns.add(campaign1);
        mockCampaigns.add(campaign2);
    }

    @Test
    void getAllCampaigns(){
        Mockito.when(campaignRepository.findAll()).thenReturn(mockCampaigns);
        List<CampaignDTO> campaignDTOs = campaignService.findAllCampaigns();
        assertEquals(mockCampaigns.size(), campaignDTOs.size());
        assertEquals(mockCampaigns.get(0).getCampaignId(), campaignDTOs.get(0).getCampaignId());
        assertEquals(mockCampaigns.get(1).getCampaignId(), campaignDTOs.get(1).getCampaignId());
    }

    @Test
    void createCampaign(){
        Mockito.when(campaignRepository.save(Mockito.any(Campaign.class))).thenReturn(campaign1);
        Mockito.when(storeRepository.findById(store.getStoreId())).thenReturn(Optional.of(store));
        Mockito.when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        CampaignDTO campaignDTO = campaignService.create(campaign1);
        assertEquals(campaignDTO.getCreatedBy().getUserId(), campaign1.getCreatedBy().getUserId());
        assertEquals(campaignDTO.getDescription(), campaign1.getDescription());
        assertEquals(campaignDTO.getStore().getStoreName(), campaign1.getStore().getStoreName());
    }

    @Test
    void updateCampaign(){
        Mockito.when(campaignRepository.save(Mockito.any(Campaign.class))).thenReturn(campaign1);
        Mockito.when(storeRepository.findById(store.getStoreId())).thenReturn(Optional.of(store));
        Mockito.when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));
        campaign1.setDescription("test update");
        CampaignDTO campaignDTO = campaignService.update(campaign1);
        assertEquals(campaignDTO.getDescription(), "test update");
    }

    @Test
    void deleteCampaign() throws Exception{
        Mockito.when(campaignRepository.findById(campaign1.getCampaignId())).thenReturn(Optional.of(campaign1));
        campaignService.delete(campaign1.getCampaignId());
        assertAll(() -> campaignService.delete(campaign1.getCampaignId()));        
    }

    @Test
    void findSingleCampaign(){
        Mockito.when(campaignRepository.findById(campaign1.getCampaignId())).thenReturn(Optional.of(campaign1));
        CampaignDTO campaignDTO = campaignService.findByCampaignId(campaign1.getCampaignId());
        assertEquals(campaignDTO.getCampaignId(), campaign1.getCampaignId());
    }
}