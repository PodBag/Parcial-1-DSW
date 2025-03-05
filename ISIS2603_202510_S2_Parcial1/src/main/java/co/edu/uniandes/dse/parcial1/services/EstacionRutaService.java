package co.edu.uniandes.dse.parcial1.services;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import co.edu.uniandes.dse.parcial1.entities.EstacionEntity;
import co.edu.uniandes.dse.parcial1.entities.RutaEntity;
import co.edu.uniandes.dse.parcial1.repositories.EstacionRepository;
import co.edu.uniandes.dse.parcial1.repositories.RutaRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class EstacionRutaService {

    @Autowired
    private EstacionRepository estacionRepository;

    @Autowired
    private RutaRepository rutaRepository;

    @Transactional
    public void addRutaAEstacion(Long estacionId, Long rutaId) {
        Optional<EstacionEntity> estacionEntity = estacionRepository.findById(estacionId);
        Optional<RutaEntity> rutaEntity = rutaRepository.findById(rutaId);
        if (estacionEntity.isEmpty()) {
            throw new EntityNotFoundException("La estación con ID " + estacionId + " no existe.");
        }
        if (rutaEntity.isEmpty()) {
            throw new EntityNotFoundException("La ruta con ID " + rutaId + " no existe.");
        }
        EstacionEntity estacion = estacionEntity.get();
        RutaEntity ruta = rutaEntity.get();
        if (estacion.getRuta_estacion().contains(ruta)) {
            throw new IllegalArgumentException("La estación ya está asociada a esta ruta.");
        }
        estacion.getRuta_estacion().add(ruta);
        ruta.getEstaciones().add(estacion);
        estacionRepository.save(estacion);
        rutaRepository.save(ruta);
    }

    @Transactional
    public void removeRutaDeEstacion(Long estacionId, Long rutaId) {
        Optional<EstacionEntity> estacionEntity = estacionRepository.findById(estacionId);
        Optional<RutaEntity> rutaEntity = rutaRepository.findById(rutaId);
        if (estacionEntity.isEmpty()) {
            throw new EntityNotFoundException("La estación con ID " + estacionId + " no existe.");
        }
        if (rutaEntity.isEmpty()) {
            throw new EntityNotFoundException("La ruta con ID " + rutaId + " no existe.");
        }
        EstacionEntity estacion = estacionEntity.get();
        RutaEntity ruta = rutaEntity.get();
        if (!estacion.getRuta_estacion().contains(ruta)) {
            throw new IllegalArgumentException("La estación no está asociada a esta ruta.");
        }
        estacion.getRuta_estacion().remove(ruta);
        ruta.getEstaciones().remove(estacion);
        estacionRepository.save(estacion);
        rutaRepository.save(ruta);
    }
}
