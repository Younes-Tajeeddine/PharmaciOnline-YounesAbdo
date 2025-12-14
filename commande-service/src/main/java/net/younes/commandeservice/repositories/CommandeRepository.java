package net.younes.commandeservice.repositories;

import net.younes.commandeservice.entite.Commande;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommandeRepository extends JpaRepository<Commande, Long> {

    List<Commande> findByCustomerId(Long customerId);}

