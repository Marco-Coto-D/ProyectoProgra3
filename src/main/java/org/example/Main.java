package org.example;

import modelo.CategoriaRecurso;
import modelo.Funcionario;
import persistencia.xml.CategoriaRecursoRepositorioXml;
import persistencia.xml.FuncionarioRepositorioXml;

public class Main {
    public static void main(String[] args) {
        CategoriaRecursoRepositorioXml categoriaRepo = new CategoriaRecursoRepositorioXml();
        categoriaRepo.guardar(new CategoriaRecurso("CAT-001", "Sala"));

        FuncionarioRepositorioXml funcionarioRepo = new FuncionarioRepositorioXml();
        funcionarioRepo.guardar(new Funcionario("111", "111", "Fabian", "8888-8888"));

        System.out.println("Categorías guardadas: " + categoriaRepo.listarTodos().size());
        System.out.println("Funcionarios guardados: " + funcionarioRepo.listarTodos().size());
    }
}