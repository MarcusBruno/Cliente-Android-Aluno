package com.ifms.tcc.marcusbruno.sisconfealuno.Models;

import java.io.Serializable;

/**
 * Created by marcus-bruno on 8/14/16.
 */
public class Disciplina implements Serializable {
    private String codigo;
    private String nome;
    private String descricao;


    public Disciplina(String codigo, String nome, String descricao) {
        this.codigo = codigo;
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}