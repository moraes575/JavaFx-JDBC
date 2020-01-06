package model.dao;

import java.util.List;
import model.entidades.Departamento;

public interface DepartamentoDAO {

    void insert(Departamento obj);

    void update(Departamento obj);

    void deleteById(Integer id);

    Departamento selectById(Integer id);

    List<Departamento> selectAll();
}
