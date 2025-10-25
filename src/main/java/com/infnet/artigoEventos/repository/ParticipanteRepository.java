package com.infnet.artigoEventos.repository;

import com.infnet.artigoEventos.model.Evento;
import com.infnet.artigoEventos.model.Participante;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParticipanteRepository extends JpaRepository<Participante, Integer> {
    boolean existsByEmailAndEvento(String email, Evento evento);
}