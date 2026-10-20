package br.edu.ifc.bikes.dto.mapper;

import br.edu.ifc.bikes.dto.UsuarioRequestDTO;
import br.edu.ifc.bikes.dto.UsuarioResponseDTO;
import br.edu.ifc.bikes.entity.Usuario;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import java.util.List;

// id, role e campos de auditoria não vêm do DTO de requisição: são definidos pela entidade/banco
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UsuarioMapper {
    Usuario toUsuario(UsuarioRequestDTO usuarioRequestDTO);
    UsuarioResponseDTO toResponse(Usuario usuario);
    List<UsuarioResponseDTO> toResponse(List<Usuario> usuarios);
}