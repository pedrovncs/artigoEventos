package com.infnet.artigoEventos.repository;

import com.infnet.artigoEventos.model.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventoRepository extends JpaRepository<Evento, Integer> {
}
