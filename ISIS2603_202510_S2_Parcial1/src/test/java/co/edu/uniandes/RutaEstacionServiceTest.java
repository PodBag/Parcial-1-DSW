package co.edu.uniandes;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uniandes.dse.parcial1.entities.EstacionEntity;
import co.edu.uniandes.dse.parcial1.entities.RutaEntity;
import co.edu.uniandes.dse.parcial1.repositories.EstacionRepository;
import co.edu.uniandes.dse.parcial1.repositories.RutaRepository;
import co.edu.uniandes.dse.parcial1.services.RutaEstacionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@DataJpaTest
@Transactional
@Import({RutaEstacionService.class})
public class RutaEstacionServiceTest {

    @Autowired
    private RutaEstacionService rutaEstacionService;

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
        entityManager.getEntityManager().createQuery("delete from EstacionEntity").executeUpdate();
        entityManager.getEntityManager().createQuery("delete from RutaEntity").executeUpdate();
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
    void testAddEstacionRuta() {
        EstacionEntity estacionEntity = estacionesList.get(1);
        RutaEntity rutaEntity = rutasList.get(1);

        rutaEstacionService.addEstacionRuta(estacionEntity.getId(), rutaEntity.getId());

        Optional<EstacionEntity> estacionOpt = estacionRepository.findById(estacionEntity.getId());
        assertTrue(estacionOpt.isPresent());
        EstacionEntity estacionBD = estacionOpt.get();

        assertTrue(estacionBD.getRuta_estacion().contains(rutaEntity));
    }

    @Test
    void testAddEstacionRutaEstacionNotFound() {
        Long estacionIdInvalido = 9999L; 
        Long rutaIdValido = rutasList.get(0).getId();

        assertThrows(EntityNotFoundException.class, () -> {
            rutaEstacionService.addEstacionRuta(estacionIdInvalido, rutaIdValido);
        });
    }

    @Test
    void testAddEstacionRutaRutaNotFound() {
        Long estacionIdValido = estacionesList.get(0).getId();
        Long rutaIdInvalido = 9999L; 

        assertThrows(EntityNotFoundException.class, () -> {
            rutaEstacionService.addEstacionRuta(estacionIdValido, rutaIdInvalido);
        });
    }

    @Test
    void testAddEstacionRutaAlreadyExists() {
        EstacionEntity estacionConRuta = estacionesList.get(0);
        RutaEntity rutaYaAsociada = rutasList.get(0);

        assertThrows(IllegalArgumentException.class, () -> {
            rutaEstacionService.addEstacionRuta(estacionConRuta.getId(), rutaYaAsociada.getId());
        });
    }

    @Test
    void testRemoveEstacionRuta() {
        EstacionEntity estacionConRuta = estacionesList.get(0);
        RutaEntity rutaYaAsociada = rutasList.get(0);

        rutaEstacionService.removeEstacionRuta(estacionConRuta.getId(), rutaYaAsociada.getId());

        EstacionEntity estacionBD = estacionRepository.findById(estacionConRuta.getId()).get();
        assertFalse(estacionBD.getRuta_estacion().contains(rutaYaAsociada));

        RutaEntity rutaBD = rutaRepository.findById(rutaYaAsociada.getId()).get();
        assertFalse(rutaBD.getEstaciones().contains(estacionConRuta));
    }

    @Test
    void testRemoveEstacionRutaEstacionNotFound() {
        Long estacionIdInvalido = 9999L;
        Long rutaValida = rutasList.get(0).getId();

        assertThrows(EntityNotFoundException.class, () -> {
            rutaEstacionService.removeEstacionRuta(estacionIdInvalido, rutaValida);
        });
    }

    @Test
    void testRemoveEstacionRutaRutaNotFound() {
        Long estacionValida = estacionesList.get(0).getId();
        Long rutaIdInvalido = 9999L;

        assertThrows(EntityNotFoundException.class, () -> {
            rutaEstacionService.removeEstacionRuta(estacionValida, rutaIdInvalido);
        });
    }

    @Test
    void testRemoveEstacionRutaNotAssociated() {
        EstacionEntity estacionSinRuta = estacionesList.get(1);
        RutaEntity rutaSinEstacion = rutasList.get(2);

        assertThrows(IllegalArgumentException.class, () -> {
            rutaEstacionService.removeEstacionRuta(estacionSinRuta.getId(), rutaSinEstacion.getId());
        });
    }
}
