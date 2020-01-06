package model.servicos;

import java.util.List;
import model.dao.DaoFactory;
import model.dao.VendedorDAO;
import model.entidades.Vendedor;

public class VendedorServico {

    private VendedorDAO dao = DaoFactory.criarVendedorDAO();

    public List<Vendedor> selectAll() {
        return dao.selectAll();
    }

    public void insertOrUpdate(Vendedor obj) {
        if (obj.getId() == null) {
            dao.insert(obj);
        } else {
            dao.update(obj);
        }
    }

    public void delete(Vendedor obj) {
        dao.deleteById(obj.getId());
    }

}
