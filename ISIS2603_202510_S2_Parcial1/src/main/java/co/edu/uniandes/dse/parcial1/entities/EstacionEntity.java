package co.edu.uniandes.dse.parcial1.entities;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Data;
import uk.co.jemos.podam.common.PodamExclude;

@Data
@Entity
public class EstacionEntity extends BaseEntity {
    private String name;
    private String direccion;
    private Integer capacidad;

    @PodamExclude
    @ManyToMany
    @JoinTable(
        name = "ruta_estacion",
        joinColumns = @JoinColumn(name = "estacion_id"),
        inverseJoinColumns = @JoinColumn(name = "ruta_id")
    )
    private List<RutaEntity> ruta_estacion;
}
