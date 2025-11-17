package com.infnet.artigoEventos.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;
//
@Data
@NoArgsConstructor
@Entity
@Table(name = "eventos")
public class Evento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nome;

    @Column(name = "data_evento", nullable = false)
    private LocalDateTime dataEvento;

    @Column(name = "local_evento", nullable = false)
    private String localEvento;

    private String descricao;

    @Column(name = "imagem_path")
    private String imagemPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEvento status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizador_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario organizador;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Participante> participantes;
}