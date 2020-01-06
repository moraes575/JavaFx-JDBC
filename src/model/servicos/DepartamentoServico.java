package model.servicos;

import java.util.List;
import model.dao.DaoFactory;
import model.dao.DepartamentoDAO;
import model.entidades.Departamento;

public class DepartamentoServico {
    
    private DepartamentoDAO dao = DaoFactory.criarDepartamentoDAO();
    
    public List<Departamento> selectAll() {
        return dao.selectAll();
    }
    
    public void insertOrUpdate(Departamento obj) {
        if (obj.getId() == null) {
            dao.insert(obj);
        } else {
            dao.update(obj);
        }
    }
    
    public void delete(Departamento obj) {
        dao.deleteById(obj.getId());
    }
    
}
