package com.thiago.ceep.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.thiago.ceep.R;
import com.thiago.ceep.dao.NotaDAO;
import com.thiago.ceep.model.Nota;
import com.thiago.ceep.ui.recycler.adapter.ListaNotasAdapter;
import com.thiago.ceep.ui.recycler.adapter.listener.OnItemClickListener;
import com.thiago.ceep.ui.recycler.helper.callback.NotaItemTouchHelperCallback;

import java.util.List;

import static com.thiago.ceep.ui.activity.NotaActivityConstantes.CHAVE_NOTA;
import static com.thiago.ceep.ui.activity.NotaActivityConstantes.CODIGO_REQUISICAO_ALTERA_NOTA;
import static com.thiago.ceep.ui.activity.NotaActivityConstantes.CODIGO_REQUISICAO_INSERE_NOTA;
import static com.thiago.ceep.ui.activity.NotaActivityConstantes.POSICAO_INVALIDA;

public class ListaNotasActivity extends AppCompatActivity {

    private ListaNotasAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_notas);

        List<Nota> todasNotas = pegaTodasNotas();
        configuraRecyclerView(todasNotas);

        configuraBotaoInsereNota();

    }

    private void configuraBotaoInsereNota() {
        TextView botaoInsereNota = findViewById(R.id.lista_notas_insere_nota);
        botaoInsereNota.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vaiParaFormularioNotaActivityInsere();
            }
        });
    }

    private void vaiParaFormularioNotaActivityInsere() {
        Intent iniciaFormularioNota = new Intent(ListaNotasActivity.this, FormularioNotaActivity.class);
        startActivityForResult(iniciaFormularioNota, CODIGO_REQUISICAO_INSERE_NOTA);
    }

    private List<Nota> pegaTodasNotas() {
        NotaDAO dao = new NotaDAO();
        for (int i = 0; i < 10; i++) {
            dao.insere(
                    new Nota("Título " + (i + 1),
                            "Descrição " + (i + 1)));
        }
        return dao.todos();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ehResultadoInsereNota(requestCode,  data)) {
            if (resultadoOk(resultCode)){
                Nota notaRecebida = (Nota) data.getSerializableExtra(CHAVE_NOTA);
                adiciona(notaRecebida);
            }
        }

        if (ehResultadoAlteraNota(requestCode, data)) {
            if (resultadoOk(resultCode)){
                Nota notaRecebida = (Nota) data.getSerializableExtra(CHAVE_NOTA);
                int posicaoRecebida = data.getIntExtra(NotaActivityConstantes.CHAVE_POSICAO, NotaActivityConstantes.POSICAO_INVALIDA);
                if (ehPosicaoValida(posicaoRecebida)){
                    altera(notaRecebida, posicaoRecebida);
                } else{
                    Toast.makeText(this,
                            "Ocorreu um problema na alteração da nota",
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void altera(Nota nota, int posicao) {
        new NotaDAO().altera(posicao, nota);
        adapter.altera(posicao, nota);
    }

    private boolean ehPosicaoValida(int posicaoRecebida) {
        return posicaoRecebida > POSICAO_INVALIDA;
    }

    private boolean ehResultadoAlteraNota(int requestCode, Intent data) {
        return ehCodigoRequisicaoAlteraNota(requestCode) &&
                temNota(data);
    }

    private boolean ehCodigoRequisicaoAlteraNota(int requestCode) {
        return requestCode == CODIGO_REQUISICAO_ALTERA_NOTA;
    }

    private void adiciona(Nota nota) {
        new NotaDAO().insere(nota);
        adapter.adiciona(nota);
    }

    private boolean ehResultadoInsereNota(int requestCode, Intent data) {
        return ehCodigoRequisicaoInsereNota(requestCode)
                && temNota(data);
    }

    private boolean temNota(Intent data) {
        return data.hasExtra(CHAVE_NOTA);
    }

    private boolean resultadoOk(int resultCode) {
        return resultCode == RESULT_OK;
    }

    private boolean ehCodigoRequisicaoInsereNota(int requestCode) {
        return requestCode == CODIGO_REQUISICAO_INSERE_NOTA;
    }

    private void configuraRecyclerView(List<Nota> todasNotas) {
        RecyclerView listaNotas = findViewById(R.id.lista_notas_recyclerview);
        configuraAdapter(todasNotas, listaNotas);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new NotaItemTouchHelperCallback(adapter));
        itemTouchHelper.attachToRecyclerView(listaNotas);
    }

    private void configuraAdapter(List<Nota> todasNotas, RecyclerView listaNotas) {
        adapter = new ListaNotasAdapter(this, todasNotas);
        listaNotas.setAdapter(adapter);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(Nota nota, int posicao) {
                vaiParaFormularioNotaActivityAltera(nota, posicao);
            }
        });
    }

    private void vaiParaFormularioNotaActivityAltera(Nota nota, int posicao) {
        Intent abreFormularioComNota = new Intent(ListaNotasActivity.this, FormularioNotaActivity.class);
        abreFormularioComNota.putExtra(CHAVE_NOTA, nota);
        abreFormularioComNota.putExtra(NotaActivityConstantes.CHAVE_POSICAO, posicao);
        startActivityForResult(abreFormularioComNota, CODIGO_REQUISICAO_ALTERA_NOTA);
    }
}