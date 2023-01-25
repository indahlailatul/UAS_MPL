package ac.id.atmaluhur.uas_mpl

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.android.volley.*
import com.android.volley.toolbox.*
import java.net.*
import androidx.recyclerview.widget.*
import org.json.*
import android.content.Intent

class MainActivity : AppCompatActivity() {
    private lateinit var url: String
    private lateinit var sr: StringRequest
    private lateinit var rq: RequestQueue

    private lateinit var rvBuku: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        title = "Perpustakaan HNP, MRRP, ILB"

        rvBuku = findViewById(R.id.rvBuku)
        val btnTambah = findViewById<Button>(R.id.btnTambah)

        rvBuku.setHasFixedSize(true)
        rvBuku.layoutManager = LinearLayoutManager(this@MainActivity)

        btnTambah.setOnClickListener {
            startActivity(Intent(this@MainActivity, EntriBuku::class.java))
        }
    }

    private fun getDefaultGateway(): String? {
        var defaultGateway: String? = null
        try {
            val enumNetworkInterface = NetworkInterface.getNetworkInterfaces()
            while(enumNetworkInterface.hasMoreElements()) {
                val networkInterface = enumNetworkInterface.nextElement()
                val enumInetAddress = networkInterface.inetAddresses
                while(enumInetAddress.hasMoreElements()) {
                    val inetAddress = enumInetAddress.nextElement()
                    if(inetAddress.isSiteLocalAddress) defaultGateway = inetAddress.hostAddress
                }
            }
        } catch(_: SocketException) {
            defaultGateway = null
        }
        return defaultGateway
    }

    override fun onStart() {
        super.onStart()
        val ipSebelumnya = ip
        if(getDefaultGateway() != null) {
            try {
                for(i in 0..255) {
                    val kepalaIp =
                        getDefaultGateway()?.substring(0, getDefaultGateway()?.lastIndexOf(".") ?: -1)
                    val ipTemp = "$kepalaIp.$i"
                    url = "http://$ipTemp/UAS/koneksi.php"
                    sr = StringRequest(Request.Method.GET, url, {
                        if(it.isNotEmpty()) {
                            ip = ipTemp
                            if(ip != ipSebelumnya) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "Terhubung ke $ip",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }, null)
                    rq = Volley.newRequestQueue(this@MainActivity)
                    rq.add(sr)
                }
            } catch(_: Exception) {
                ip = "192.168.106.197"
            }
        } else ip = "192.168.106.197"
        tampilData()
    }

    override fun onResume() {
        super.onResume()
        tampilData()
    }

    private fun tampilData() {
        val listBuku = arrayListOf<Buku>()
        val adapter = AdapterBuku(listBuku, this@MainActivity)

        url = "http://$ip/UAS/tampil.php"
        sr = StringRequest(Request.Method.GET, url, {
            try {
                val obj = JSONObject(it)
                val array = obj.getJSONArray("data")
                for(i in 0 until array.length()) {
                    val ob = array.getJSONObject(i)
                    with(ob) {
                        listBuku.add(Buku(
                            getString("isbn"),
                            getString("jdl_buku"),
                            getString("pengarang"),
                            getString("penerbit"),
                            getInt("thn_terbit"),
                            getString("tmpt_terbit"),
                            getInt("cetakan_ke"),
                            getString("jmlh_hal"),
                            getString("klasifikasi"),

                            ))
                    }
                }
                rvBuku.adapter = adapter
            } catch(_: JSONException) {
                Toast.makeText(this@MainActivity, "Tidak ada data...", Toast.LENGTH_LONG).show()
            }
        }, null)
        rq = Volley.newRequestQueue(this@MainActivity)
        rq.add(sr)
    }
}