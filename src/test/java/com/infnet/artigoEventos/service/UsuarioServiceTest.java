package com.infnet.artigoEventos.service;

import com.infnet.artigoEventos.dto.LoginDto;
import com.infnet.artigoEventos.dto.UsuarioDto;
import com.infnet.artigoEventos.dto.UsuarioUpdateDto;
import com.infnet.artigoEventos.model.Usuario;
import com.infnet.artigoEventos.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UsuarioServiceTest {
    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private UsuarioDto usuarioDto;

    @BeforeEach
    void setup() {
        usuarioRepository.deleteAll();

        usuarioDto = new UsuarioDto();
        usuarioDto.setNome("Nome Teste");
        usuarioDto.setEmail("email@test.com");
        usuarioDto.setSenha("123456");
        usuarioService.signup(usuarioDto);
    }

    @Test
    public void singUpTest() {
        UsuarioDto usuarioDtoSignUp = new UsuarioDto();
        usuarioDtoSignUp.setNome("Nome Teste SignUp");
        usuarioDtoSignUp.setEmail("emailSignUp@test.com");
        usuarioDtoSignUp.setSenha("1234567");
        Usuario usuario = usuarioService.signup(usuarioDtoSignUp);

        assertNotNull(usuario);
    }

    @Test
    public void signupEmailJaCadastradoTest() {
        assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.signup(usuarioDto);
        });
    }

    @Test
    public void loginTest() {
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail(usuarioDto.getEmail());
        loginDto.setSenha(usuarioDto.getSenha());

        Usuario usuario = usuarioService.login(loginDto);

        assertNotNull(usuario);
    }

    @Test
    public void loginSenhaInvalidaTest() {
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail(usuarioDto.getEmail());
        loginDto.setSenha("1234");

        assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.login(loginDto);
        });
    }

    @Test
    public void updateUsuarioTest() {
        UsuarioUpdateDto updateDto = new UsuarioUpdateDto();
        updateDto.setNome(usuarioDto.getNome() + "updateTest");
        updateDto.setSenha(usuarioDto.getSenha() + "7654321");

        Usuario atualizado = usuarioService.updateUsuario(1, updateDto);

        assertEquals(usuarioDto.getNome() + "updateTest", atualizado.getNome());
        assertNotEquals(usuarioDto.getSenha() + "7654321", atualizado.getSenha());
    }

    @Test
    public void updateUsuarioNaoEncontradoTest() {
        UsuarioUpdateDto updateDto = new UsuarioUpdateDto();
        updateDto.setNome(usuarioDto.getNome() + "updateTestError");
        updateDto.setSenha(usuarioDto.getSenha() + "7654321");

        assertThrows(EntityNotFoundException.class, () -> {
            usuarioService.updateUsuario(-10, updateDto);
        });
    }

    @Test
    public void deleteUsuarioTest() {
        assertDoesNotThrow(() -> usuarioService.deleteUsuario(1));
    }

    @Test
    public void deleteUsuarioNaoEncontradoTest() {
        assertThrows(EntityNotFoundException.class, () -> {
            usuarioService.deleteUsuario(-10);
        });
    }

    @Test
    public void loadUserByUsernameTest() {
        UserDetails user = usuarioService.loadUserByUsername("email@test.com");

        assertEquals("email@test.com", user.getUsername());
        assertNotNull(user.getPassword());
    }

    @Test
    public void loadUserByUsernameNaoEncontradoTest() {
        assertThrows(UsernameNotFoundException.class, () -> {
            usuarioService.loadUserByUsername("naoExiste@test.com");
        });
    }
import static org.junit.jupiter.api.Assertions.*;

class UsuarioServiceTest {

}