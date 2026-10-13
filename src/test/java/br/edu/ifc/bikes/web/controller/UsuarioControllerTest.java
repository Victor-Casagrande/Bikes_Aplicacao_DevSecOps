package br.edu.ifc.bikes.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioControllerTest {

    private static final String URL = "/api/v1/usuarios";

    @Autowired
    private MockMvc mockMvc;

    private String criarUsuario(String username) throws Exception {
        String body = mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"123456\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.role").value("CLIENTE"))
                .andReturn().getResponse().getContentAsString();
        return body.replaceAll(".*\"id\":(\\d+).*", "$1");
    }

    private static String emailUnico() {
        return "user-" + UUID.randomUUID() + "@bikes.com";
    }

    @Test
    void criaUsuarioValidoRetorna201() throws Exception {
        criarUsuario(emailUnico());
    }

    @Test
    void usuarioInvalidoRetorna422ComErrosPorCampo() throws Exception {
        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"nao-e-email\",\"password\":\"123\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    void buscaPorIdExistenteRetorna200() throws Exception {
        String username = emailUnico();
        String id = criarUsuario(username);

        mockMvc.perform(get(URL + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username));
    }

    @Test
    void buscaPorIdInexistenteRetorna404() throws Exception {
        mockMvc.perform(get(URL + "/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Usuário com id=999999 não encontrado"));
    }

    @Test
    void usernameDuplicadoRetorna409() throws Exception {
        String username = emailUnico();
        criarUsuario(username);

        mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"123456\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    private ResultActions alterarSenha(String id, String atual, String nova, String confirmacao) throws Exception {
        return mockMvc.perform(patch(URL + "/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"senhaAtual\":\"" + atual + "\",\"novaSenha\":\"" + nova
                        + "\",\"confirmaSenha\":\"" + confirmacao + "\"}"));
    }

    @Test
    void alteraSenhaValidaRetorna204() throws Exception {
        String id = criarUsuario(emailUnico());

        alterarSenha(id, "123456", "654321", "654321")
                .andExpect(status().isNoContent());

        // a senha antiga deixa de valer e a nova passa a ser a atual
        alterarSenha(id, "123456", "111111", "111111")
                .andExpect(status().isBadRequest());
        alterarSenha(id, "654321", "111111", "111111")
                .andExpect(status().isNoContent());
    }

    @Test
    void senhaAtualIncorretaRetorna400() throws Exception {
        String id = criarUsuario(emailUnico());

        alterarSenha(id, "000000", "654321", "654321")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Sua senha atual não confere"));
    }

    @Test
    void confirmacaoDiferenteRetorna400() throws Exception {
        String id = criarUsuario(emailUnico());

        alterarSenha(id, "123456", "654321", "999999")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Nova senha não confere com a confirmação de senha"));
    }

    @Test
    void alteraSenhaComCamposVaziosRetorna422() throws Exception {
        String id = criarUsuario(emailUnico());

        alterarSenha(id, "", "", "")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errors.senhaAtual").exists())
                .andExpect(jsonPath("$.errors.novaSenha").exists())
                .andExpect(jsonPath("$.errors.confirmaSenha").exists());
    }

    @Test
    void alteraSenhaComMenosDe6CaracteresRetorna422() throws Exception {
        String id = criarUsuario(emailUnico());

        alterarSenha(id, "12345", "12345", "12345")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errors.senhaAtual").exists())
                .andExpect(jsonPath("$.errors.novaSenha").exists())
                .andExpect(jsonPath("$.errors.confirmaSenha").exists());
    }

    @Test
    void alteraSenhaComMaisDe6CaracteresRetorna422() throws Exception {
        String id = criarUsuario(emailUnico());

        alterarSenha(id, "1234567", "1234567", "1234567")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.errors.senhaAtual").exists())
                .andExpect(jsonPath("$.errors.novaSenha").exists())
                .andExpect(jsonPath("$.errors.confirmaSenha").exists());
    }

    @Test
    void alteraSenhaDeUsuarioInexistenteRetorna404() throws Exception {
        alterarSenha("999999", "123456", "654321", "654321")
                .andExpect(status().isNotFound());
    }

    @Test
    void listaUsuariosRetorna200() throws Exception {
        criarUsuario(emailUnico());

        mockMvc.perform(get(URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
