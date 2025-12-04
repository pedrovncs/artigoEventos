package com.infnet.artigoEventos.service;

import com.infnet.artigoEventos.dto.LoginDto;
import com.infnet.artigoEventos.dto.UsuarioDto;
import com.infnet.artigoEventos.dto.UsuarioUpdateDto;
import com.infnet.artigoEventos.model.Usuario;
import com.infnet.artigoEventos.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.ArrayList;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario signup(UsuarioDto usuarioDto) {
        if (usuarioRepository.findByEmail(usuarioDto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email ja cadastrado");
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(usuarioDto.getNome());
        novoUsuario.setEmail(usuarioDto.getEmail());
        novoUsuario.setSenha(passwordEncoder.encode(usuarioDto.getSenha()));

        return usuarioRepository.save(novoUsuario);
    }

    public Usuario login(LoginDto loginDto) {
        Usuario usuario = usuarioRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("Usuario nao encontrado"));

        if (!passwordEncoder.matches(loginDto.getSenha(), usuario.getSenha())) {
            throw new IllegalArgumentException("Senha invalida");
        }
        return usuario;
    }

    public Usuario updateUsuario(Integer id, UsuarioUpdateDto dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario nao encontrado com id: " + id));

        if (dto.getNome() != null) {
            usuario.setNome(dto.getNome());
        }

        if (dto.getSenha() != null && !dto.getSenha().isEmpty()) {
            usuario.setSenha(passwordEncoder.encode(dto.getSenha()));
        }

        return usuarioRepository.save(usuario);
    }

    public void deleteUsuario(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new EntityNotFoundException("Usuario nao encontrado com id: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com email: " + email));

        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getSenha(),
                new ArrayList<>()
        );
    }

    //metodo adicionado para pull request da apresentacao apresentacao
    public void doNothing(){
        System.out.println("fazendo nada");
    }
}