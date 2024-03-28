package sg.edu.nus.iss.springboot.voucher.management.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.nus.iss.springboot.voucher.management.dto.CampaignDTO;
import sg.edu.nus.iss.springboot.voucher.management.dto.UserRequest;
import sg.edu.nus.iss.springboot.voucher.management.entity.Campaign;
import sg.edu.nus.iss.springboot.voucher.management.entity.Store;
import sg.edu.nus.iss.springboot.voucher.management.entity.User;
import sg.edu.nus.iss.springboot.voucher.management.enums.CampaignStatus;
import sg.edu.nus.iss.springboot.voucher.management.enums.RoleType;
import sg.edu.nus.iss.springboot.voucher.management.service.impl.CampaignService;
import sg.edu.nus.iss.springboot.voucher.management.utility.DTOMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "DB_USERNAME=admin",
        "DB_PASSWORD=RDS_12345",
        "AWS_ACCESS_KEY=AKIA47CRXTTV2EHMAA3S",
        "AWS_SECRET_KEY=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",
        "AES_SECRET_KEY=",
        "FRONTEND_URL="
})
public class CampaignControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private CampaignService campaignService;


        private static List<CampaignDTO> mockCampaigns = new ArrayList<>();
        private static User user = new User("1", "test@email.com", "username", "pwd", RoleType.CUSTOMER, null, null,
                        true,
                        null, null, null, null, null, null, null, null, false);
       
        private static Store store = new Store("1", "MUJI",
    			"MUJI offers a wide variety of good quality items from stationery to household items and apparel.", "",
    			"Test", "#04-36/40 Paragon Shopping Centre", "290 Orchard Rd", "", "238859", "Singapore", "Singapore",
    			"Singapore", "123456", null, user, null, user, false, null);
        
        private static Campaign campaign1 = new Campaign("1", "new campaign 1", store, CampaignStatus.CREATED, null, 10,
                        0,
                        null, null, 10, LocalDateTime.now(), LocalDateTime.now(), user, user, LocalDateTime.now(),
                        LocalDateTime.now(), null,false);
        private static Campaign campaign2 = new Campaign("2", "new campaign 2", store, CampaignStatus.CREATED, null, 10,
                        0,
                        null, null, 10, LocalDateTime.now(), LocalDateTime.now(), user, user, LocalDateTime.now(),
                        LocalDateTime.now(), null,false);

        @BeforeAll
        static void setUp() {
                mockCampaigns.add(DTOMapper.toCampaignDTO(campaign1));
                mockCampaigns.add(DTOMapper.toCampaignDTO(campaign2));
        }

        @Test
        void testGetAllActiveCampaigns() throws Exception {
                Mockito.when(campaignService.findAllActiveCampaigns()).thenReturn(mockCampaigns);
                mockMvc.perform(MockMvcRequestBuilders.get("/api/campaign/all/active")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data[0].campaignId").value(1)).andDo(print());
        }

        @Test
        void getAllCampaignsByStoreId() throws Exception {
                Mockito.when(campaignService.findAllCampaignsByStoreId(store.getStoreId())).thenReturn(mockCampaigns);
                mockMvc.perform(MockMvcRequestBuilders.post("/api/campaign/getAllByStoreId")
                                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(store)))
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data[0].campaignId").value(1)).andDo(print());
        }

        @Test
        void testGetAllCampaignsByEmail() throws Exception {
                Mockito.when(campaignService.findAllCampaignsByEmail(campaign1.getCreatedBy().getEmail())).thenReturn(mockCampaigns);
                mockMvc.perform(MockMvcRequestBuilders.post("/api/campaign/getAllByEmail")
                                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(user)))
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.success").value(true)).andDo(print());
        }
     
        /*
        @Test
        void testCreateCampaign() throws Exception {
                Mockito.when(campaignService.create(campaign1)).thenReturn(DTOMapper.toCampaignDTO(campaign1));
               
                mockMvc.perform(MockMvcRequestBuilders.post("/api/campaign/create")
                                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(campaign1)))
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.success").value(true)).andDo(print());
        }

        @Test
        void testUpdateCampaign() throws Exception {
                campaign1.setDescription("new desc");
               
                Mockito.when(campaignService.update(campaign1)).thenReturn(DTOMapper.toCampaignDTO(campaign1));
                
                mockMvc.perform(MockMvcRequestBuilders.post("/api/campaign/update")
                                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(campaign1)))
                                .andExpect(MockMvcResultMatchers.status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$.success").value(true)).andDo(print());
                // .andExpect(jsonPath("$data.description").value("new desc")).andDo(print());
        }

       
        @Test
    	void testPromoteCampaign() throws Exception {    
        	campaign1.setCampaignStatus(CampaignStatus.READYTOPROMOTE);
        	Mockito.when(campaignService.promote(campaign1)).thenReturn(DTOMapper.toCampaignDTO(campaign1));
           	mockMvc.perform(MockMvcRequestBuilders.post("/api/campaign/promote")
    				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(campaign1)))
    				.andExpect(MockMvcResultMatchers.status().isOk())
    				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
    				.andExpect(jsonPath("$.success").value(true)).andDo(print());
    	}
     */

}
