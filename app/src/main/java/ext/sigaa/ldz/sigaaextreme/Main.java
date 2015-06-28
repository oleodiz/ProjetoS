package ext.sigaa.ldz.sigaaextreme;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.akexorcist.roundcornerprogressbar.IconRoundCornerProgressBar;
import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;

import java.util.ArrayList;
import java.util.concurrent.Executors;

public class Main extends ActionBarActivity {

    class LoadListener{
        @android.webkit.JavascriptInterface
        public void processHTML(String html)
        {
            //txt_html.setText(html);
            //Log.e("HTML", html);
            if (pagina == PaginaAtual.LOGIN && !comErro)
                new VerificaErros().executeOnExecutor(Executors.newFixedThreadPool(4), html);
            if (pagina == PaginaAtual.PORTAL_DISCENTE && txt_nome == null)
                new CarregaDadosPerfil().executeOnExecutor(Executors.newFixedThreadPool(4), html);
            //if (pagina == PaginaAtual.PORTAL_DISCENTE)
            //    processaImagem();
        }

        @android.webkit.JavascriptInterface
        public void getImage(String imageBase64)
        {
            new CarregaImagem().executeOnExecutor(Executors.newFixedThreadPool(4), imageBase64);
        }
    }
    boolean podeProcessar;
    //OBJETOS GERAIS
    public enum PaginaAtual{LOGIN, MENU_PRINCIPAL, PORTAL_DISCENTE};
    boolean comErro;
    FrameLayout area_geral;
    WebView web;
    PaginaAtual pagina;
    SharedPreferences dadosSalvos;
    SharedPreferences.Editor editor;
    LayoutInflater layoutInflater;
    View vw_login, vw_perfil;
    //OBJETOS LOGIN
    EditText edt_usuario, edt_senha;
    Button btn_entrar;
    NiftyDialogBuilder dialogo;
    //OBJETOS DISCENTE
    TextView  txt_nome,txt_curso, txt_matricula, txt_email, txt_nivel, txt_status, txt_ingresso, txt_entrada, txt_mgp, txt_regularidade, txt_percentual;
    ImageView img_perfil;
    IconRoundCornerProgressBar pgr_percentual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geral);
        //CONSTRUINDO OBJETOS GERAIS
        area_geral = (FrameLayout) findViewById(R.id.fra_area_geral);
        web = (WebView) findViewById(R.id.web_acessoWeb);
        dadosSalvos = this.getPreferences(Context.MODE_PRIVATE);
        editor = dadosSalvos.edit();
        comErro = false;
        dialogo= NiftyDialogBuilder.getInstance(this);

        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
                web.setWebContentsDebuggingEnabled(true);
            }
        }

        web.addJavascriptInterface(new LoadListener(), "METODOS");
        web.setWebChromeClient(new WebChromeClient());

        web.loadUrl("https://www.sigaa.ufs.br/sigaa/verTelaLogin.do");
        podeProcessar = true;
        pagina = PaginaAtual.LOGIN;
        layoutInflater = (LayoutInflater)
                this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        vw_login = layoutInflater.inflate(R.layout.login, area_geral);
        //area_geral.addView(vw_login);
        constroiObjetosLogin();

        web.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url,
                                      Bitmap favicon) {
            }

            public void onPageFinished(WebView view, String url) {

                if (podeProcessar) {
                    ArrayList<String> scripts = new ArrayList<String>();
                    scripts.add("window.METODOS.processHTML(document.getElementsByTagName('html')[0].innerHTML);");
                    executaScripts(scripts);
                    if (url.contains("verMenuPrincipal")) {
                        pagina = PaginaAtual.MENU_PRINCIPAL;
                        web.loadUrl("https://www.sigaa.ufs.br/sigaa/verPortalDiscente.do");

                    } else {
                        if (url.contains("discente.jsf")) {

                            if (pagina != PaginaAtual.PORTAL_DISCENTE) {
                                pagina = PaginaAtual.PORTAL_DISCENTE;

                                //podeProcessar = false;
                            }
                        } else {
                            pagina = PaginaAtual.LOGIN;
                        }
                    }
                }
            }
        });
    }

    private void ExibeDialog(String mensagem, boolean cancelavel, boolean cancelavelFora, int resourse, boolean animado) {

        dialogo
                .withTitle(mensagem)
                .withTitleColor("#FFFFFF")
                .withDividerColor("#80BBF0")
                .withDialogColor("#FFFFFF")
                .withDuration(700)
                .withEffect(Effectstype.Fadein)
                .isCancelableOnTouchOutside(cancelavelFora)
                .isCancelable(cancelavel)
                .withImg(resourse, animado).show();

        dialogo.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                comErro = false;
            }
        });
    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void constroiObjetosLogin()
    {
        btn_entrar = (Button) vw_login.findViewById(R.id.btn_entrar);
        edt_usuario = (EditText) vw_login.findViewById(R.id.edt_usuario);
        edt_senha = (EditText) vw_login.findViewById(R.id.edt_senha);
        edt_usuario.setText(dadosSalvos.getString("USUARIO", ""));

        btn_entrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String usuario = edt_usuario.getText().toString();
                String senha = edt_senha.getText().toString();

                ArrayList<String> scripts = new ArrayList<String>();
                scripts.add("document.getElementById('usuario').value='" + usuario + "';");
                scripts.add("document.getElementById('senha').value='" + senha + "';");
                scripts.add("document.getElementsByName('loginForm')[0].submit();");
                executaScripts(scripts);
                podeProcessar = true;
                editor.putString("USUARIO", usuario);
                editor.commit();
                hideKeyboard();
                ExibeDialog("Entrando...", false, false, R.drawable.entrando, true);
            }
        });

    }

    private void constroiObjetosPerfil()
    {
        txt_nome = (TextView) vw_perfil.findViewById(R.id.txt_nome);
        img_perfil = (ImageView) vw_perfil.findViewById(R.id.img_perfil);
        txt_curso = (TextView) vw_perfil.findViewById(R.id.txt_curso);
        txt_matricula = (TextView) vw_perfil.findViewById(R.id.txt_matricula);
        txt_email = (TextView) vw_perfil.findViewById(R.id.txt_email);
        txt_nivel = (TextView) vw_perfil.findViewById(R.id.txt_nivel);
        txt_status = (TextView) vw_perfil.findViewById(R.id.txt_status);
        txt_ingresso = (TextView) vw_perfil.findViewById(R.id.txt_ingresso);
        txt_entrada = (TextView) vw_perfil.findViewById(R.id.txt_entrada);
        txt_mgp = (TextView) vw_perfil.findViewById(R.id.txt_mgp);
        txt_regularidade = (TextView) vw_perfil.findViewById(R.id.txt_regularidade);
        txt_percentual = (TextView) vw_perfil.findViewById(R.id.txt_percentual);
        pgr_percentual = (IconRoundCornerProgressBar) vw_perfil.findViewById(R.id.pgr_percentual);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void processaImagem()
    {
        ArrayList<String> scripts = new ArrayList<String>();
        scripts.add("document.body.innerHTML += '<canvas id=\"canvas\"></canvas>';");
        scripts.add("var c = document.getElementById(\"canvas\");");
        scripts.add("var ctx = c.getContext(\"2d\");");
        scripts.add("var div = document.getElementsByClassName('foto')[0];");
        scripts.add("var img = div.getElementsByTagName(\"img\")[0];");
        scripts.add("ctx.drawImage(img, 0, 0);");
        scripts.add("window.METODOS.getImage(c.toDataURL());");

        executaScripts(scripts);
    }

    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedByte = Base64.decode(input, 0);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);

        return Bitmap.createBitmap(bitmap, 0, 0, 100, 125);
    }

    public void executaScripts(ArrayList<String> scripts)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String scriptsUnidos ="";
            for (int i =0; i < scripts.size(); i++)
                scriptsUnidos +=scripts.get(i);
            web.evaluateJavascript("javascript:"+scriptsUnidos, null);
        } else {
            for (int i =0; i < scripts.size(); i++)
                web.loadUrl("javascript:"+scripts.get(i));
        }
    }


    private class CarregaImagem extends AsyncTask<String, String, Boolean> {
        @Override
        protected Boolean doInBackground(String... img64) {
            String imagem = img64[0].split(",")[1];

            publishProgress(imagem);
          return true;
        }
        @Override
        protected void onProgressUpdate(String... imagem) {
            img_perfil.setImageBitmap(decodeBase64(imagem[0]));
            //img_perfil.setVisibility(View.VISIBLE);

        }
    }

    private class CarregaDadosPerfil extends AsyncTask<String, String, Boolean> {
        @Override
        protected Boolean doInBackground(String... html) {

            String areaPerfil, nome, matricula, curso, cidade, sexo, nivel, status, email, ingresso, semestre, MGP, regularidade, percentual;
            areaPerfil = nome = matricula = sexo =nivel = status = email = ingresso = semestre = cidade = curso = MGP = regularidade =  percentual = "";

            int auxPerfil = html[0].indexOf("class=\"nome\"");
            if (auxPerfil == -1)
                return false;
            areaPerfil = html[0].substring(auxPerfil);
            areaPerfil = areaPerfil.substring(0, areaPerfil.indexOf("main-docente"));

            nome = areaPerfil.substring(areaPerfil.indexOf("<b>") + 3, areaPerfil.indexOf("</b>"));

            matricula = areaPerfil.substring(areaPerfil.indexOf("Matrícula:") + 10, areaPerfil.indexOf("Curso:"));
            matricula = limpaString(matricula);

            curso = areaPerfil.substring(areaPerfil.indexOf("Curso:") + 12, areaPerfil.indexOf("Nível:"));
            curso = limpaString(curso);
            String[] CCS = curso.split("-");
            curso = CCS[0];
            cidade = CCS[1];
            sexo = CCS[2];

            nivel = areaPerfil.substring(areaPerfil.indexOf("Nível:") + 12, areaPerfil.indexOf("Status:"));
            nivel = limpaString(nivel);

            status = areaPerfil.substring(areaPerfil.indexOf("Status:") + 13, areaPerfil.indexOf("E-Mail:"));
            status = limpaString(status);

            email = areaPerfil.substring(areaPerfil.indexOf("E-Mail:") + 13, areaPerfil.indexOf("Entrada:"));
            email = limpaString(email);

            semestre = areaPerfil.substring(areaPerfil.indexOf("Entrada:") + 14, areaPerfil.indexOf("Ingresso:"));
            semestre = limpaString(semestre);

            ingresso = areaPerfil.substring(areaPerfil.indexOf("Ingresso:") + 15, areaPerfil.indexOf("Ingresso:") + 45);
            ingresso = limpaString(ingresso);

            MGP = areaPerfil.substring(areaPerfil.indexOf("MGP:") + 82, areaPerfil.indexOf("MGP:") + 86);
            MGP = MGP.replace("<", "");

            regularidade = areaPerfil.substring(areaPerfil.indexOf("IREG:") + 83);
            regularidade = regularidade.substring(0, regularidade.indexOf("<"));
            regularidade = regularidade.replace("<", "");

            percentual = areaPerfil.substring(areaPerfil.indexOf("Integralizado") - 5, areaPerfil.indexOf("Integralizado"));
            percentual = limpaString(percentual);
            //               0       1         2      3     4    5      6       7       8         9       10     11            12
            publishProgress(nome, matricula, curso,cidade,sexo,email, nivel, status, semestre, ingresso, MGP, regularidade, percentual);
            return true;
        }
        @Override
        protected void onProgressUpdate(String... dadosPerfil) {

            dialogo.dismiss();
            area_geral.removeViewAt(1);
            vw_perfil = layoutInflater.inflate(R.layout.perfil, area_geral);

            constroiObjetosPerfil();
            processaImagem();

            txt_nome.setText(dadosPerfil[0]);
            txt_matricula.setText(dadosPerfil[1]);
            txt_curso.setText(dadosPerfil[2] + " - "+ dadosPerfil[3]);
            txt_email.setText(dadosPerfil[5]);
            txt_nivel.setText(dadosPerfil[6]);
            txt_status.setText(dadosPerfil[7]);
            txt_entrada.setText(dadosPerfil[8]);
            txt_ingresso.setText(dadosPerfil[9]);
            txt_mgp.setText(dadosPerfil[10]);
            txt_regularidade.setText(dadosPerfil[11]);
            txt_percentual.setText(dadosPerfil[12]);
            pgr_percentual.setProgress(Float.parseFloat(dadosPerfil[12].replace("%", "")));

        }

        protected String limpaString(String sujo)
        {
            return sujo.replace("<td>","").replace("</td>","").replace("<tr>", "").replace("</tr>", "").replace("\n","").replace("\t", "").trim();
        }
    }

    private class VerificaErros extends AsyncTask<String, String, Boolean> {
        int codErro =0;
        @Override
        protected Boolean doInBackground(String... html) {

            if (html[0].contains("Usuário e/ou senha inválidos")) {
                codErro =1;
                publishProgress("Usuário e/ou senha inválidos!");
            }
            if (html[0].contains("Você está off-line")
                    || html[0].contains("Não foi possível acessar a rede")
                    || html[0].contains("Página da web não disponível")
                    || html[0].contains("Esta página da web não está disponível"))
            {
                codErro = 2;
                publishProgress("Sem conexão com a internet!");
            }

            return true;
        }
        @Override
        protected void onProgressUpdate(String... mensagem) {
            switch (codErro)
            {
                case 1: ExibeDialog(mensagem[0], true, true, R.drawable.acessonegado, false); break;
                case 2: ExibeDialog(mensagem[0], true, true, R.drawable.semrede, false); break;

                default: break;
            }
            codErro =0;
            comErro = true;

        }
    }
}
