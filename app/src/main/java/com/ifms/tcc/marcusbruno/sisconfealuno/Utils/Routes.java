package com.ifms.tcc.marcusbruno.sisconfealuno.Utils;

/**
 * Created by marcus-bruno on 8/18/16.
 */
public class Routes {
    private static  final String URL = "http://192.168.1.12:8000/";
    private static final String URL_LOGIN_ALUNO = URL +"todo/login/aluno/";
    private static final String URL_BUSCAR_CHAMADA_ABERTA = URL+"todo/aluno/chamada/";
    private static final String URL_BUSCAR_DISCIPLINAS_ALUNO = URL+"todo/disciplinas/aluno/";
    private static final String URL_AUTENTICAR_PRESENÇA = URL+ "todo/aluno/atenticar/presenca/";
    private static final String URL_CADASTRAR_ALUNO = URL+ "todo/aluno/cadastrar/";
    private static final String URL_BUSCAR_LISTA_FREQUENCIA_ALUNO = URL+ "todo/aluno/listafrequencia/";
    private static final String URL_RECUPERAR_SENHA_ALUNO = URL+ "todo/aluno/recuperar/senha/";

    public static String getUrlLoginAluno() {
        return URL_LOGIN_ALUNO;
    }

    public static String getUrlBuscarChamadaAberta() {
        return URL_BUSCAR_CHAMADA_ABERTA;
    }

    public static String getUrlBuscarDisciplinasAluno() { return URL_BUSCAR_DISCIPLINAS_ALUNO; }

    public static String getUrlAutenticarPresença() { return URL_AUTENTICAR_PRESENÇA; }

    public static String getUrlCadastrarAluno() {
        return URL_CADASTRAR_ALUNO;
    }

    public static String getUrlBuscarListaFrequenciaAluno() {
        return URL_BUSCAR_LISTA_FREQUENCIA_ALUNO;
    }

    public static String getUrlRecuperarSenhaAluno() {
        return URL_RECUPERAR_SENHA_ALUNO;
    }
}
