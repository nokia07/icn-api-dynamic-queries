package co.com.portalup.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.com.portalup.security.DesignerAccessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DesignerAccessController.class)
class DesignerAccessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DesignerAccessService designerAccessService;

    @Test
    void userInTheTableCanDesign() throws Exception {
        when(designerAccessService.canDesign()).thenReturn(true);

        mockMvc.perform(get("/api/v1/designer-access"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canDesign").value(true));
    }

    // Es un GET: el interceptor no lo bloquea aunque el usuario no tenga permiso, solo responde false.
    @Test
    void userOutsideTheTableCannotDesign() throws Exception {
        when(designerAccessService.canDesign()).thenReturn(false);

        mockMvc.perform(get("/api/v1/designer-access"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.canDesign").value(false));
    }
}
