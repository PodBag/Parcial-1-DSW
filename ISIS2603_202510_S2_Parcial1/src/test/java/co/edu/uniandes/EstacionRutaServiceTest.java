package co.edu.uniandes;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import co.edu.uniandes.dse.parcial1.entities.EstacionEntity;
import co.edu.uniandes.dse.parcial1.entities.RutaEntity;
import co.edu.uniandes.dse.parcial1.repositories.EstacionRepository;
import co.edu.uniandes.dse.parcial1.repositories.RutaRepository;
import co.edu.uniandes.dse.parcial1.services.EstacionRutaService;
import jakarta.persistence.EntityNotFoundException;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@DataJpaTest
@Transactional
@Import({ EstacionRutaService.class })
public class EstacionRutaServiceTest {

    @Autowired
    private EstacionRutaService estacionRutaService;

    @Autowired
    private EstacionRepository estacionRepository;

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private PodamFactory factory = new PodamFactoryImpl();

    private List<EstacionEntity> estacionesList = new ArrayList<>();
    private List<RutaEntity> rutasList = new ArrayList<>();

    @BeforeEach
    void setUp() {
        clearData();
        insertData();
    }

    private void clearData() {
        entityManager.getEntityManager().createQuery("DELETE FROM EstacionEntity").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM RutaEntity").executeUpdate();
    }

    private void insertData() {
        for (int i = 0; i < 3; i++) {
            EstacionEntity estacionEntity = factory.manufacturePojo(EstacionEntity.class);
            entityManager.persist(estacionEntity);
            estacionesList.add(estacionEntity);
        }
        for (int i = 0; i < 3; i++) {
            RutaEntity rutaEntity = factory.manufacturePojo(RutaEntity.class);
            entityManager.persist(rutaEntity);
            rutasList.add(rutaEntity);
        }
        EstacionEntity estacionConRuta = estacionesList.get(0);
        RutaEntity rutaYaAsociada = rutasList.get(0);
        estacionConRuta.getRuta_estacion().add(rutaYaAsociada);
        rutaYaAsociada.getEstaciones().add(estacionConRuta);
        entityManager.merge(estacionConRuta);
        entityManager.merge(rutaYaAsociada);
    }

    @Test
    void testAddRutaAEstacion() {
        EstacionEntity estacionNoAsociada = estacionesList.get(1);
        RutaEntity rutaNoAsociada = rutasList.get(1);
        estacionRutaService.addRutaAEstacion(estacionNoAsociada.getId(), rutaNoAsociada.getId());
        EstacionEntity estacionDB = estacionRepository.findById(estacionNoAsociada.getId()).get();
        assertTrue(estacionDB.getRuta_estacion().contains(rutaNoAsociada));
    }

    @Test
    void testAddRutaAEstacionEstacionNotFound() {
        Long estacionInexistente = 9999L;
        Long rutaValida = rutasList.get(0).getId();
        assertThrows(EntityNotFoundException.class, () -> {
            estacionRutaService.addRutaAEstacion(estacionInexistente, rutaValida);
        });
    }

    @Test
    void testAddRutaAEstacionRutaNotFound() {
        Long estacionValida = estacionesList.get(0).getId();
        Long rutaInexistente = 9999L;
        assertThrows(EntityNotFoundException.class, () -> {
            estacionRutaService.addRutaAEstacion(estacionValida, rutaInexistente);
        });
    }

    @Test
    void testAddRutaAEstacionAlreadyExists() {
        EstacionEntity estacionAsociada = estacionesList.get(0);
        RutaEntity rutaAsociada = rutasList.get(0);
        assertThrows(IllegalArgumentException.class, () -> {
            estacionRutaService.addRutaAEstacion(estacionAsociada.getId(), rutaAsociada.getId());
        });
    }

    @Test
    void testRemoveRutaDeEstacion() {
        EstacionEntity estacionAsociada = estacionesList.get(0);
        RutaEntity rutaAsociada = rutasList.get(0);
        estacionRutaService.removeRutaDeEstacion(estacionAsociada.getId(), rutaAsociada.getId());
        EstacionEntity estacionDB = estacionRepository.findById(estacionAsociada.getId()).get();
        RutaEntity rutaDB = rutaRepository.findById(rutaAsociada.getId()).get();
        assertFalse(estacionDB.getRuta_estacion().contains(rutaAsociada));
        assertFalse(rutaDB.getEstaciones().contains(estacionAsociada));
    }

    @Test
    void testRemoveRutaDeEstacionEstacionNotFound() {
        Long estacionInexistente = 9999L;
        Long rutaValida = rutasList.get(0).getId();
        assertThrows(EntityNotFoundException.class, () -> {
            estacionRutaService.removeRutaDeEstacion(estacionInexistente, rutaValida);
        });
    }

    @Test
    void testRemoveRutaDeEstacionRutaNotFound() {
        Long estacionValida = estacionesList.get(0).getId();
        Long rutaInexistente = 9999L;
        assertThrows(EntityNotFoundException.class, () -> {
            estacionRutaService.removeRutaDeEstacion(estacionValida, rutaInexistente);
        });
    }

    @Test
    void testRemoveRutaDeEstacionNotAssociated() {
        EstacionEntity estacionNoAsociada = estacionesList.get(1);
        RutaEntity rutaNoAsociada = rutasList.get(2);
        assertThrows(IllegalArgumentException.class, () -> {
            estacionRutaService.removeRutaDeEstacion(estacionNoAsociada.getId(), rutaNoAsociada.getId());
        });
    }
}

