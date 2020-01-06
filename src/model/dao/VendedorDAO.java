package model.dao;

import java.util.List;
import model.entidades.Departamento;
import model.entidades.Vendedor;

public interface VendedorDAO {

    void insert(Vendedor obj);

    void update(Vendedor obj);

    void deleteById(Integer id);

    Vendedor selectById(Integer id);

    List<Vendedor> selectAll();

    List<Vendedor> selectByDepartamento(Departamento departamento);
}
