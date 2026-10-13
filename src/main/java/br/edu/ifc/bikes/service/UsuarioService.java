package br.edu.ifc.bikes.service;

import br.edu.ifc.bikes.dto.UsuarioRequestDTO;
import br.edu.ifc.bikes.dto.UsuarioResponseDTO;
import br.edu.ifc.bikes.dto.mapper.UsuarioMapper;
import br.edu.ifc.bikes.entity.Usuario;
import br.edu.ifc.bikes.exception.EntityNotFoundException;
import br.edu.ifc.bikes.exception.PasswordInvalidException;
import br.edu.ifc.bikes.exception.UsernameUniqueViolationException;
import br.edu.ifc.bikes.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    public UsuarioResponseDTO create(UsuarioRequestDTO usuarioRequestDTO) {
        try {
            Usuario usuario = usuarioMapper.toUsuario(usuarioRequestDTO);
            Usuario salvo = usuarioRepository.save(usuario);
            log.info("Usuário criado: id={}", salvo.getId()); // e-mail (PII) não é logado
            return usuarioMapper.toResponse(salvo);
        } catch (DataIntegrityViolationException ex) {
            // Só a violação de unicidade vira 409; outras falhas de banco continuam como erro 500
            throw new UsernameUniqueViolationException(String.format("O nome do usuário '%s' já existe", usuarioRequestDTO.username()));
        }
    }

    @Transactional()
    public UsuarioResponseDTO getById(Long id) {
        log.debug("Buscando usuário id={}", id);
        return usuarioMapper.toResponse(buscarPorId(id));
    }

    @Transactional()
    public void updatePassword(Long id, String senhaAtual, String novaSenha, String confirmaSenha) {
        if (!novaSenha.equals(confirmaSenha)) {
            throw new PasswordInvalidException("Nova senha não confere com a confirmação de senha");
        }

        Usuario usuario = buscarPorId(id);
        if (!usuario.getPassword().equals(senhaAtual)) {
            throw new PasswordInvalidException("Sua senha atual não confere");
        }

        usuario.setPassword(novaSenha);
        log.info("Senha atualizada: id={}", id);
    }

    public List<UsuarioResponseDTO> getAll() {
        List<Usuario> usuarios = usuarioRepository.findAll();
        log.debug("Listagem de usuários: {} registro(s)", usuarios.size());
        return usuarioMapper.toResponse(usuarios);
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format("Usuário com id=%d não encontrado", id))
        );
    }
}
