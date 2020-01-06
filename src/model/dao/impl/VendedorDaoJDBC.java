package model.dao.impl;

import db.DB;
import db.DbException;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.dao.VendedorDAO;
import model.entidades.Departamento;
import model.entidades.Vendedor;

public class VendedorDaoJDBC implements VendedorDAO {

    private Connection conn;

    public VendedorDaoJDBC(Connection conn) {
        this.conn = conn;
    }

    @Override
    public void insert(Vendedor obj) {

        PreparedStatement st = null;

        try {
            st = conn.prepareStatement("INSERT INTO vendedor "
                    + "(nome, email, dataNascimento, salarioBase, departamentoFK) "
                    + "VALUES "
                    + "(?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);

            st.setString(1, obj.getNome());
            st.setString(2, obj.getEmail());
            st.setDate(3, new java.sql.Date(obj.getDataNascimento().getTime()));
            st.setDouble(4, obj.getSalarioBase());
            st.setInt(5, obj.getDepartamento().getId());

            int linhasAfetadas = st.executeUpdate();

            if (linhasAfetadas > 0) {
                ResultSet rs = st.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    obj.setId(id);
                }
                DB.closeResultSet(rs);
            } else {
                throw new DbException("Erro inesperado! Nenhuma linha foi afetada!");
            }
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public void update(Vendedor obj) {

        PreparedStatement st = null;

        try {
            st = conn.prepareStatement("UPDATE vendedor SET "
                    + "nome = ?, email = ?, dataNascimento = ?, salarioBase = ?, departamentoFK = ? "
                    + "WHERE ID = ?");

            st.setString(1, obj.getNome());
            st.setString(2, obj.getEmail());
            st.setDate(3, new java.sql.Date(obj.getDataNascimento().getTime()));
            st.setDouble(4, obj.getSalarioBase());
            st.setInt(5, obj.getDepartamento().getId());
            st.setInt(6, obj.getId());

            st.executeUpdate();

        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public void deleteById(Integer id) {

        PreparedStatement st = null;

        try {
            st = conn.prepareStatement("DELETE FROM vendedor WHERE ID = ?");

            st.setInt(1, id);
            st.executeUpdate();

        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.closeStatement(st);
        }
    }

    @Override
    public Vendedor selectById(Integer id) {

        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            st = conn.prepareStatement("SELECT vendedor.*, departamento.nome AS depNome "
                    + "FROM vendedor INNER JOIN departamento "
                    + "ON vendedor.departamentoFK = departamento.ID "
                    + "WHERE vendedor.ID = ?");

            st.setInt(1, id);
            rs = st.executeQuery();

            if (rs.next()) {
                Departamento dep = instanciarDepartamento(rs);
                Vendedor obj = instanciarVendedor(rs, dep);
                return obj;
            }
            return null;

        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(st);
        }

    }

    @Override
    public List<Vendedor> selectAll() {

        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            st = conn.prepareStatement("SELECT vendedor.*, departamento.nome AS depNome "
                    + "FROM vendedor INNER JOIN departamento "
                    + "ON vendedor.departamentoFK = departamento.ID "
                    + "ORDER BY ID");

            rs = st.executeQuery();

            List<Vendedor> lista = new ArrayList<>();
            Map<Integer, Departamento> map = new HashMap<>();

            while (rs.next()) {

                Departamento dep = map.get(rs.getInt("departamentoFK"));

                if (dep == null) {
                    dep = instanciarDepartamento(rs);
                    map.put(rs.getInt("departamentoFK"), dep);
                }

                Vendedor obj = instanciarVendedor(rs, dep);
                lista.add(obj);
            }
            return lista;

        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(st);
        }
    }

    @Override
    public List<Vendedor> selectByDepartamento(Departamento departamento) {

        PreparedStatement st = null;
        ResultSet rs = null;

        try {
            st = conn.prepareStatement("SELECT vendedor.*, departamento.nome AS depNome "
                    + "FROM vendedor INNER JOIN departamento "
                    + "ON vendedor.departamentoFK = departamento.ID "
                    + "WHERE departamentoFK = ? "
                    + "ORDER BY nome");

            st.setInt(1, departamento.getId());
            rs = st.executeQuery();

            List<Vendedor> lista = new ArrayList<>();
            Map<Integer, Departamento> map = new HashMap<>();

            while (rs.next()) {

                Departamento dep = map.get(rs.getInt("departamentoFK"));

                if (dep == null) {
                    dep = instanciarDepartamento(rs);
                    map.put(rs.getInt("departamentoFK"), dep);
                }

                Vendedor obj = instanciarVendedor(rs, dep);
                lista.add(obj);
            }
            return lista;

        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        } finally {
            DB.closeResultSet(rs);
            DB.closeStatement(st);
        }
    }

    private Departamento instanciarDepartamento(ResultSet rs) throws SQLException {

        Departamento dep = new Departamento();
        dep.setId(rs.getInt("departamentoFK"));
        dep.setNome(rs.getString("depNome"));
        return dep;
    }

    private Vendedor instanciarVendedor(ResultSet rs, Departamento dep) throws SQLException {

        Vendedor obj = new Vendedor();
        obj.setId(rs.getInt("id"));
        obj.setNome(rs.getString("nome"));
        obj.setEmail(rs.getString("email"));
        obj.setSalarioBase(rs.getDouble("salarioBase"));
        obj.setDataNascimento(new java.util.Date(rs.getTimestamp("dataNascimento").getTime()));
        obj.setDepartamento(dep);
        return obj;
    }

}
