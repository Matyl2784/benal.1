package com.example.benal_10
import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.*
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.Room
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.benal_10.ui.theme.Benal_10Theme
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.background
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.DrawerState
import androidx.compose.ui.draw.clip
import kotlinx.coroutines.CoroutineScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.material.icons.outlined.Add
import kotlin.String
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Save
import androidx.compose.foundation.selection.selectable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.*
import kotlinx.coroutines.flow.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.material.icons.filled.History
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.sp

val WBcolors = listOf(0xffEBEBEB, 0xffF6F5F4, 0xff0f213c, 0xff1c3c6c, 0xff1d70a2)

var Acolor = WBcolors[0]
var Bcolor = WBcolors[1]
var Ccolor = WBcolors[2]
var Dcolor = WBcolors[3]
var Ecolor = WBcolors[4]


@Entity(tableName = "rides")
data class Ride(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val car: String,
    val date: String,
    val startTime: Long,
    val startKm: Int? = null,
    val startFuelKm: Int? = null,
    val isFinished: Boolean = false,
    val endTime: Long? = null,
    val endKm: Int? = null,
    val endFuelKm: Int? = null,
    val distance: Int? = null,
    val fuel: Float? = null,
    val price: Float? = null,
    val averageSpeed: Int? = null,
    val rideTime: Int? = null, //v sekundach
    val isPersonal: Boolean = false,
    val isFamily: Boolean = false,
    val isBoys: Boolean = false,
    val isReimburses: Boolean = false,
    val from: String? = null,
    val destination: String? = null,
    val notes: String? = null
)

@Dao
interface RideDao {

    // Živý seznam všech jízd (na UI použijeme .collectAsState())
    @Query("SELECT * FROM rides ORDER BY id DESC")
    fun observeAllRides(): Flow<List<Ride>>

    // Živá „poslední nedokončená“ jízda (nebo null)
    @Query("SELECT * FROM rides ORDER BY id DESC LIMIT 1")
    fun observeLastRide(): Flow<Ride?>

    @Insert
    suspend fun insertRide(ride: Ride): Long

    @Query("SELECT id FROM rides WHERE isFinished = 0 ORDER BY id DESC LIMIT 1")
    suspend fun getUnfinishedRide(): Int

    @Update
    suspend fun updateRide(ride: Ride)

    @Query("SELECT * FROM rides")
    suspend fun getAllRides(): List<Ride>
}



@Database(entities = [Ride::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rideDao(): RideDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    // POZOR: pro vývoj. Zničí data při změně schématu.
                    .build()
                    .also { INSTANCE = it }
            }
    }
}

class CounterViewModel: ViewModel() {
    var ActualID by mutableIntStateOf(0)
    var car by mutableStateOf("")
    var date by mutableStateOf("")
    var startTime by mutableLongStateOf(0L)
    var startKm by mutableIntStateOf(0)
    var startFuelKm by mutableIntStateOf(0)
    var isFinished by mutableStateOf(false)
    var endTime by mutableLongStateOf(0L)
    var endKm by mutableIntStateOf(0)
    var endFuelKm by mutableIntStateOf(0)
    var distance by mutableIntStateOf(0)
    var fuel by mutableFloatStateOf(0F)
    var price by mutableFloatStateOf(0F)
    var averageSpeed by mutableIntStateOf(0)
    var rideTime by mutableIntStateOf(1)
    var isPersonal by mutableStateOf(false)
    var isFamily by mutableStateOf(false)
    var isBoys by mutableStateOf(false)
    var isReimburses by mutableStateOf(false)
    var from by mutableStateOf("")
    var destination by mutableStateOf("")
    var notes by mutableStateOf("")

    var lastEndKm by mutableIntStateOf(0)

    var lastEndFuelKm by mutableIntStateOf(0)

    var lastActualID by mutableIntStateOf(-1)

    var new_click by mutableIntStateOf(0)

    var ulozit by mutableIntStateOf(0)

    var wasNewRide by mutableIntStateOf(0)

    var restarovat by mutableIntStateOf(0)

    var ulozeniNewRide by mutableIntStateOf(0)

    var selectedOption by mutableStateOf("Personal")
    var message by mutableStateOf("")




}


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            Benal_10Theme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()


                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .padding(top = 35.dp), drawerContainerColor = Color(Ccolor)) {
                            Text("Text", modifier = Modifier.padding(16.dp))
                            HorizontalDivider()
                            NavigationDrawerItem(
                                label = { Text("Item") },
                                selected = false,
                                onClick = {}
                            )
                        }
                    }
                ){
                Scaffold(modifier = Modifier.fillMaxSize(),
                    containerColor = Color(Acolor),
                    topBar = {
                        MyTopAppBar(scope, drawerState)
                    }) { innerPadding ->

                    Column(modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                        MessageSnackbar()
                        Nacteni()
                        RideInfoCard()
                        Mezera(mezera = 5)
                        Prvni()
                        Mezera(mezera = 25)
                        Druhy()
                        Mezera(mezera = 25)
                        Treti()
                        Mezera(mezera = 25)
                        Restart()
                        Info()
                    }
                }
            }}
        }
    }
}


@ExperimentalMaterial3Api
@Composable
fun MyTopAppBar(
    scope: CoroutineScope,
    drawerState: DrawerState
) {

    val viewModel: CounterViewModel = viewModel()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val start = if (viewModel.startTime == 0L) null else formatTime(viewModel.startTime)
    val end = if (viewModel.endTime == 0L) null else formatTime(viewModel.endTime)

    val result = when {
        start == null && end == null -> "nová jízda"
        start != null && end == null -> "$start - X"
        start != null && end != null -> "$start - $end"
        else -> "Error"
    }

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color(Dcolor),
            titleContentColor = Color(Bcolor),
        ),
        title = {
            Text(
                "Ford ride: ${viewModel.ActualID}, ${result}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 10.dp)
            )
        },
        navigationIcon = {
            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Localized description"
                )
            }
        },
        actions = {
            IconButton(onClick = { viewModel.new_click += 1 }) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Localized description"
                )
            }
        },
        scrollBehavior = scrollBehavior,
    )
    if (viewModel.new_click == 1) {
        Nova_jizda()
        viewModel.new_click = 2
    }
}



@Composable
fun Mezera(mezera: Int) {
    Box(modifier = Modifier.height(mezera.dp))
}


@Composable
fun Nacteni() {
    val context = LocalContext.current
    val viewModel: CounterViewModel = viewModel()
    val rideDao = remember {
        AppDatabase.getDatabase(context).rideDao()
    } // Optimalizace, aby se DAO nevytvářelo při každé rekompozici

    LaunchedEffect(key1 = rideDao) {
        val lastRideObject =
            rideDao.observeLastRide().firstOrNull() // Získá aktuální hodnotu z Flow
        if (lastRideObject != null) {
            viewModel.ActualID = lastRideObject.id
            viewModel.car = lastRideObject.car
            viewModel.date = lastRideObject.date
            viewModel.startTime = lastRideObject.startTime
            viewModel.startKm =
                lastRideObject.startKm ?: 0 // Ošetření pro případ, že startKm je null
            viewModel.startFuelKm = lastRideObject.startFuelKm ?: 0
            viewModel.isFinished = lastRideObject.isFinished
            viewModel.endTime = lastRideObject.endTime ?: 0
            viewModel.endKm = lastRideObject.endKm ?: 0
            viewModel.endFuelKm = lastRideObject.endFuelKm ?: 0
            viewModel.isPersonal = lastRideObject.isPersonal
            viewModel.isFamily = lastRideObject.isFamily
            viewModel.isBoys = lastRideObject.isBoys
            viewModel.isReimburses = lastRideObject.isReimburses
            viewModel.from = lastRideObject.from ?: ""
            viewModel.destination = lastRideObject.destination ?: ""
            viewModel.notes = lastRideObject.notes ?: ""
            viewModel.lastActualID = lastRideObject.id
            viewModel.wasNewRide = 0
            viewModel.new_click = 0
        } else {
            viewModel.ActualID = 0 // Nebo nějaká jiná indikace
        }
    }
    if (viewModel.ActualID == 0) {
        Nova_jizda()
        viewModel.new_click == 2
    }
    if (viewModel.isFinished == true && viewModel.ulozeniNewRide == 0) {
        Nova_jizda()
        viewModel.new_click == 2
    }

    if (viewModel.isPersonal == true) {
        viewModel.selectedOption = "Personal"
    }
    if (viewModel.isFamily == true) {
        viewModel.selectedOption = "Family"
    }
    if (viewModel.isBoys == true) {
        viewModel.selectedOption = "Boys"
    }


}
@Composable
fun RideInfoCard() {
    val viewModel: CounterViewModel = viewModel()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color(0xFFF5F5F5), RoundedCornerShape(15.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Datum jízdy: ${viewModel.date}",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF333333)
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (viewModel.endTime == 0L && viewModel.startTime != 0L) {
            Text(
                text = "Aktuální Start KM: ${viewModel.startKm}",
                fontSize = 16.sp,
                color = Color(0xFF555555)
            )
            Text(
                text = "Aktuální Start Fuel KM: ${viewModel.startFuelKm}",
                fontSize = 16.sp,
                color = Color(0xFF555555)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "DOKONČI JÍZDU!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
        } else {
            Text(
                text = "End KM z minulé jízdy: ${viewModel.lastEndKm}",
                fontSize = 16.sp,
                color = Color(0xFF555555)
            )
            Text(
                text = "End Fuel KM z minulé jízdy: ${viewModel.lastEndFuelKm}",
                fontSize = 16.sp,
                color = Color(0xFF555555)
            )
        }
    }
}


//nacteni neuspesne (neni zadna jizda) - actual = 1, last = 0
//nacteni uspesne (nacetlo to finished jizdu) - actual = 11, last = 10
//nacteni uspesne (nacetlo to nefinished jizdu) - actual = 10, last = 10
//nacteni uspesne (nacetlo to nefinished jizdu ale dam novou jizdu) - actual = 11, last = 10

@Composable
fun Nova_jizda(){
    val viewModel: CounterViewModel = viewModel()

    viewModel.lastEndKm = viewModel.endKm
    viewModel.lastEndFuelKm = viewModel.endFuelKm
    viewModel.lastActualID = viewModel.ActualID

    viewModel.car = "Ford Focus"
    viewModel.date = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
    viewModel.startTime = 0
    viewModel.isFinished = false
    viewModel.endTime = 0
    viewModel.isPersonal = true
    viewModel.isFamily = false
    viewModel.isBoys = false
    viewModel.isReimburses = false
    viewModel.from = ""
    viewModel.destination = ""
    viewModel.notes = ""
    viewModel.startKm = 0
    viewModel.startFuelKm = 0
    viewModel.endKm = 0
    viewModel.endFuelKm = 0
    viewModel.ActualID = viewModel.ActualID + 1

    viewModel.wasNewRide = 1
    viewModel.new_click = 2
}

@Composable
fun Ulozeni() {
    val viewModel: CounterViewModel = viewModel()
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val rideDao = db.rideDao()

    viewModel.startTime = System.currentTimeMillis()

    if (viewModel.endKm != 0 || viewModel.endFuelKm != 0) {
        viewModel.ulozeniNewRide = 1
        viewModel.isFinished = true
        viewModel.endTime = System.currentTimeMillis()
        Vypocty()
    }

    if (viewModel.selectedOption == "Personal"){
        viewModel.isPersonal = true
        viewModel.isFamily = false
        viewModel.isBoys = false
    }
    if (viewModel.selectedOption == "Family"){
        viewModel.isFamily = true
        viewModel.isPersonal = false
        viewModel.isBoys = false
    }
    if (viewModel.selectedOption == "Boys"){
        viewModel.isBoys = true
        viewModel.isPersonal = false
        viewModel.isFamily = false
    }

    if (viewModel.ActualID == viewModel.lastActualID) { //update
        LaunchedEffect(viewModel.ActualID) {
            val updatedRide = Ride(
                id = viewModel.ActualID,
                car = viewModel.car,
                date = viewModel.date,
                startTime = viewModel.startTime,
                startKm = viewModel.startKm,
                startFuelKm = viewModel.startFuelKm,
                isFinished = viewModel.isFinished,
                endTime = viewModel.endTime,
                endKm = viewModel.endKm,
                endFuelKm = viewModel.endFuelKm,
                distance = viewModel.distance,
                fuel = viewModel.fuel,
                price = viewModel.price,
                averageSpeed = viewModel.averageSpeed,
                rideTime = viewModel.rideTime,
                isPersonal = viewModel.isPersonal,
                isFamily = viewModel.isFamily,
                isBoys = viewModel.isBoys,
                isReimburses = viewModel.isReimburses,
                from = viewModel.from,
                destination = viewModel.destination,
                notes = viewModel.notes
            )
            rideDao.updateRide(updatedRide)
        }
    }

    if (viewModel.wasNewRide == 1) { //new ride
        if (viewModel.startKm != 0 || viewModel.startFuelKm != 0){
            LaunchedEffect(Unit) {
                val newRide = Ride(
                    //id = viewModel.ActualID,
                    car = viewModel.car,
                    date = viewModel.date,
                    startTime = viewModel.startTime,
                    startKm = viewModel.startKm,
                    startFuelKm = viewModel.startFuelKm,
                    isFinished = viewModel.isFinished,
                    endTime = viewModel.endTime,
                    endKm = viewModel.endKm,
                    endFuelKm = viewModel.endFuelKm,
                    distance = viewModel.distance,
                    fuel = viewModel.fuel,
                    price = viewModel.price,
                    averageSpeed = viewModel.averageSpeed,
                    rideTime = viewModel.rideTime,
                    isPersonal = viewModel.isPersonal,
                    isFamily = viewModel.isFamily,
                    isBoys = viewModel.isBoys,
                    isReimburses = viewModel.isReimburses,
                    from = viewModel.from,
                    destination = viewModel.destination,
                    notes = viewModel.notes
                )
                val newId = rideDao.insertRide(newRide).toInt()
                viewModel.ActualID = newId
            }
        viewModel.wasNewRide = 0
        viewModel.lastActualID = viewModel.ActualID
    }
    }
    viewModel.message = "Succeed! ✅" // zpráva
    viewModel.new_click = 0
}

@Composable
fun MessageSnackbar() {
    val viewModel: CounterViewModel = viewModel()
    if (viewModel.message.isNotEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .wrapContentHeight()
                .shadow(8.dp, RoundedCornerShape(16.dp))
                .background(Color(0xFF4CAF50), RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = viewModel.message,
                    color = Color.White,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        LaunchedEffect(viewModel.message) {
            kotlinx.coroutines.delay(1575) // zobrazí 3,5 sekundy
            viewModel.message = ""
        }
    }
}



@Composable
fun Vypocty(){
    val viewModel: CounterViewModel = viewModel()

    if (viewModel.startKm != 0 || viewModel.endKm != 0) {
        viewModel.distance = viewModel.endKm - viewModel.startKm
    }
    else {
        viewModel.distance = viewModel.startFuelKm - viewModel.endFuelKm
    }
    viewModel.rideTime = ((viewModel.endTime - viewModel.startTime) / 1000).toInt()
    viewModel.averageSpeed = (viewModel.distance.toFloat() / (viewModel.rideTime.toFloat() / 60 / 60)).toInt()
    if (viewModel.startFuelKm != 0 || viewModel.endFuelKm != 0) {
        viewModel.fuel = (viewModel.startFuelKm - viewModel.endFuelKm).toFloat()
    }
    else {
        viewModel.fuel = viewModel.distance.toFloat()
    }
    viewModel.price = (viewModel.fuel / 9.7F) * 34

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Prvni() {
    val viewModel: CounterViewModel = viewModel()


    Box(modifier = Modifier
        .shadow(10.dp, RoundedCornerShape(36.dp))
        .fillMaxWidth(0.88f)
        .clip(RoundedCornerShape(36.dp))
        .background(Color(Bcolor))
        .height(150.dp)){
        Column{
        Row {
            var checked by rememberSaveable { mutableStateOf(false) }
            var localTextFieldValue by remember { mutableStateOf(TextFieldValue("")) }
            LaunchedEffect(Unit) { // Spustí se jen jednou na začátku
                val initialFormattedValue = formatInGroupsOfThreeFromEnd(viewModel.startKm.toString())
                localTextFieldValue = TextFieldValue(text = initialFormattedValue, selection = TextRange(initialFormattedValue.length))
            }

            OutlinedTextField(
                value = localTextFieldValue,
                onValueChange = { newValue ->
                    val rawText = newValue.text.replace(" ", "")
                    val formatted = formatInGroupsOfThreeFromEnd(rawText)
                    localTextFieldValue = TextFieldValue(text = formatted, selection = TextRange(formatted.length))

                    // Aktualizace ViewModelu
                    // Použij očištěný text (bez mezer) pro konverzi na Int
                    val cleanedText = formatted.replace(" ", "")
                    viewModel.startKm = cleanedText.toIntOrNull() ?: 0 // Aktualizuj startKm ve ViewModelu
                    // Použij ?: 0 pro případ, že konverze selže nebo je text prázdný
                },
                modifier = Modifier
                    .width(175.dp)
                    .padding(start = 13.dp, top = 6.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(Dcolor),
                    unfocusedBorderColor = Color(Ecolor), cursorColor = Color(Dcolor),
                    focusedLabelColor = Color(Dcolor), unfocusedTextColor = Color(Ecolor),
                    focusedTextColor = Color(Ecolor), disabledTextColor = Color(0xffcccccc),
                    disabledBorderColor = Color(Ecolor),
                    disabledLabelColor = Color(0xFFAAAAAA), unfocusedLabelColor = Color(Ccolor)
                ),
                shape = RoundedCornerShape(30.dp),
                placeholder = { Text("Start KM") },
                enabled = !checked,
                label = { Text("Start KM") },
                //leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            )
            IconToggleButton(
                checked = checked,
                onCheckedChange = { checked = it; viewModel.startKm = viewModel.lastEndKm },
                colors = IconButtonDefaults.iconToggleButtonColors(checkedContentColor = Color(0xff888888)),
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(size = 60.dp)
            ) {
                if (checked) {
                    Icon(Icons.Filled.Edit, contentDescription = "Localized description")
                } else {
                    Icon(Icons.Filled.History, contentDescription = "Localized description")
                }
            }
            IconButton(onClick = { viewModel.ulozit += 1 }, modifier = Modifier
                .padding(top = 20.dp, end = 0.dp, start = 25.dp)
                .size(40.dp)) {
                Icon(
                    imageVector = Icons.Filled.Save,
                    contentDescription = "Localized description", modifier = Modifier.size(40.dp)
                )
            }
        }
        Row (modifier = Modifier.padding(top = 7.dp).fillMaxWidth(1f)) {
            var checked by rememberSaveable { mutableStateOf(false) }
            var localTextFieldValue by remember { mutableStateOf(TextFieldValue("")) }
            LaunchedEffect(Unit) { // Spustí se jen jednou na začátku
                val initialFormattedValue = formatInGroupsOfThreeFromEnd(viewModel.startFuelKm.toString())
                localTextFieldValue = TextFieldValue(text = initialFormattedValue, selection = TextRange(initialFormattedValue.length))
            }
            OutlinedTextField(
                value = localTextFieldValue,
                onValueChange = { newValue ->
                    val rawText = newValue.text.replace(" ", "")
                    val formatted = formatInGroupsOfThreeFromEnd(rawText)
                    localTextFieldValue = TextFieldValue(text = formatted, selection = TextRange(formatted.length))

                    // Aktualizace ViewModelu
                    // Použij očištěný text (bez mezer) pro konverzi na Int
                    val cleanedText = formatted.replace(" ", "")
                    viewModel.startFuelKm = cleanedText.toIntOrNull() ?: 0 // Aktualizuj startKm ve ViewModelu
                    // Použij ?: 0 pro případ, že konverze selže nebo je text prázdný
                },
                modifier = Modifier
                    .width(175.dp)
                    .padding(start = 13.dp, bottom = 16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(Dcolor),
                    unfocusedBorderColor = Color(Ecolor), cursorColor = Color(Dcolor),
                    focusedLabelColor = Color(Dcolor), unfocusedTextColor = Color(Ecolor),
                    focusedTextColor = Color(Ecolor), disabledTextColor = Color(0xffcccccc),
                    disabledBorderColor = Color(Ecolor),
                    disabledLabelColor = Color(0xFFAAAAAA), unfocusedLabelColor = Color(Ccolor)
                ),
                shape = RoundedCornerShape(30.dp),
                placeholder = { Text("Start Fuel KM") },
                enabled = !checked,
                label = { Text("Start Fuel KM") }
                //leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            )
            IconToggleButton(
                checked = checked,
                onCheckedChange = { checked = it; viewModel.startFuelKm = viewModel.lastEndFuelKm },
                colors = IconButtonDefaults.iconToggleButtonColors(
                    checkedContentColor = Color(
                        0xff888888
                    )
                ),
                modifier = Modifier
                    .padding(top = 3.dp)
                    .size(size = 60.dp)
            ) {
                if (checked) {
                    Icon(Icons.Filled.Edit, contentDescription = "Localized description")
                } else {
                    Icon(Icons.Filled.History, contentDescription = "Localized description")
                }
            }

            var showDialog by remember { mutableStateOf(false) }
            var selectedTime by remember { mutableStateOf("Teď") }

            IconButton(onClick = { showDialog = true }, modifier = Modifier
                .padding(top = 10.dp, end = 0.dp, start = 25.dp)
                .size(40.dp)) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Vybrat čas", modifier = Modifier.size(40.dp),
                    tint = Color(Ccolor)
                )
            }

            if (showDialog) {
                Dialog(onDismissRequest = { showDialog = false }) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        tonalElevation = 6.dp,
                    ) {
                        // Tady použiješ TimePicker z Material3
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .wrapContentHeight()
                        ) {
                            val state = rememberTimePickerState()
                            TimePicker(state = state)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(onClick = { showDialog = false }) {
                                    Text("Zrušit")
                                }
                                Button(onClick = {
                                    selectedTime = "%02d:%02d".format(state.hour, state.minute)
                                    showDialog = false
                                    viewModel.startTime = selectedTime.toLong()
                                }) {
                                    Text("Potvrdit")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    }
    if (viewModel.ulozit == 1) {
        Ulozeni()
        viewModel.ulozit = 0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Druhy() {
    val viewModel: CounterViewModel = viewModel()

    Box(modifier = Modifier
        .shadow(10.dp, RoundedCornerShape(36.dp))
        .fillMaxWidth(0.88f)
        .clip(RoundedCornerShape(36.dp))
        .background(Color(Bcolor))
        .height(150.dp)){
        Column{
            Row {
                var checked by rememberSaveable { mutableStateOf(false) }
                var localTextFieldValue by remember { mutableStateOf(TextFieldValue("")) }
                LaunchedEffect(Unit) { // Spustí se jen jednou na začátku
                    val initialFormattedValue = formatInGroupsOfThreeFromEnd(viewModel.endKm.toString())
                    localTextFieldValue = TextFieldValue(text = initialFormattedValue, selection = TextRange(initialFormattedValue.length))
                }


                OutlinedTextField(
                    value = localTextFieldValue,
                    onValueChange = { newValue ->
                        val rawText = newValue.text.replace(" ", "")
                        val formatted = formatInGroupsOfThreeFromEnd(rawText)
                        localTextFieldValue = TextFieldValue(text = formatted, selection = TextRange(formatted.length))

                        // Aktualizace ViewModelu
                        // Použij očištěný text (bez mezer) pro konverzi na Int
                        val cleanedText = formatted.replace(" ", "")
                        viewModel.endKm = cleanedText.toIntOrNull() ?: 0 // Aktualizuj startKm ve ViewModelu
                        // Použij ?: 0 pro případ, že konverze selže nebo je text prázdný
                    },
                    modifier = Modifier
                        .width(175.dp)
                        .padding(start = 13.dp, top = 6.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(Dcolor),
                        unfocusedBorderColor = Color(Ecolor), cursorColor = Color(Dcolor),
                        focusedLabelColor = Color(Dcolor), unfocusedTextColor = Color(Ecolor),
                        focusedTextColor = Color(Ecolor), disabledTextColor = Color(0xffcccccc),
                        disabledBorderColor = Color(Ecolor),
                        disabledLabelColor = Color(0xFFAAAAAA), unfocusedLabelColor = Color(Ccolor)
                    ),
                    shape = RoundedCornerShape(30.dp),
                    placeholder = { Text("End KM") },
                    enabled = !checked,
                    label = { Text("End KM") },
                    //leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                )
                IconToggleButton(
                    checked = checked,
                    onCheckedChange = { checked = it },
                    colors = IconButtonDefaults.iconToggleButtonColors(checkedContentColor = Color(0xff888888)),
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .size(size = 60.dp)
                ) {
                    if (checked) {
                        Icon(Icons.Filled.Edit, contentDescription = "Localized description")
                    } else {
                        Icon(Icons.Filled.Check, contentDescription = "Localized description")
                    }
                }
                IconButton(onClick = { viewModel.ulozit += 1 }, modifier = Modifier
                    .padding(top = 20.dp, end = 0.dp, start = 25.dp)
                    .size(40.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = "Localized description", modifier = Modifier.size(40.dp)
                    )
                }
            }
            Row (modifier = Modifier.padding(top = 7.dp)) {
                var checked by rememberSaveable { mutableStateOf(false) }
                var localTextFieldValue by remember { mutableStateOf(TextFieldValue("")) }
                LaunchedEffect(Unit) { // Spustí se jen jednou na začátku
                    val initialFormattedValue = formatInGroupsOfThreeFromEnd(viewModel.endFuelKm.toString())
                    localTextFieldValue = TextFieldValue(text = initialFormattedValue, selection = TextRange(initialFormattedValue.length))
                }
                OutlinedTextField(
                    value = localTextFieldValue,
                    onValueChange = { newValue ->
                        val rawText = newValue.text.replace(" ", "")
                        val formatted = formatInGroupsOfThreeFromEnd(rawText)
                        localTextFieldValue = TextFieldValue(text = formatted, selection = TextRange(formatted.length))

                        // Aktualizace ViewModelu
                        // Použij očištěný text (bez mezer) pro konverzi na Int
                        val cleanedText = formatted.replace(" ", "")
                        viewModel.endFuelKm = cleanedText.toIntOrNull() ?: 0 // Aktualizuj startKm ve ViewModelu
                        // Použij ?: 0 pro případ, že konverze selže nebo je text prázdný
                    },
                    modifier = Modifier
                        .width(175.dp)
                        .padding(start = 13.dp, bottom = 16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(Dcolor),
                        unfocusedBorderColor = Color(Ecolor), cursorColor = Color(Dcolor),
                        focusedLabelColor = Color(Dcolor), unfocusedTextColor = Color(Ecolor),
                        focusedTextColor = Color(Ecolor), disabledTextColor = Color(0xffcccccc),
                        disabledBorderColor = Color(Ecolor),
                        disabledLabelColor = Color(0xFFAAAAAA), unfocusedLabelColor = Color(Ccolor)
                    ),
                    shape = RoundedCornerShape(30.dp),
                    placeholder = { Text("End Fuel KM") },
                    enabled = !checked,
                    label = { Text("End Fuel KM") }
                    //leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                )
                IconToggleButton(
                    checked = checked,
                    onCheckedChange = { checked = it },
                    colors = IconButtonDefaults.iconToggleButtonColors(
                        checkedContentColor = Color(
                            0xff888888
                        )
                    ),
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .size(size = 60.dp)
                ) {
                    if (checked) {
                        Icon(Icons.Filled.Edit, contentDescription = "Localized description")
                    } else {
                        Icon(Icons.Filled.Check, contentDescription = "Localized description")
                    }
                }


                var showDialog by remember { mutableStateOf(false) }
                var selectedTime by remember { mutableStateOf("Teď") }


                IconButton(onClick = { showDialog = true }, modifier = Modifier
                    .padding(top = 10.dp, end = 0.dp, start = 25.dp)
                    .size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Vybrat čas", modifier = Modifier.size(40.dp),
                        tint = Color(Ccolor)
                    )
                }

                if (showDialog) {
                    Dialog(onDismissRequest = { showDialog = false }) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            tonalElevation = 6.dp,
                        ) {
                            // Tady použiješ TimePicker z Material3
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .wrapContentHeight()
                            ) {
                                val state = rememberTimePickerState()
                                TimePicker(state = state)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(onClick = { showDialog = false }) {
                                        Text("Zrušit")
                                    }
                                    Button(onClick = {
                                        selectedTime = "%02d:%02d".format(state.hour, state.minute)
                                        showDialog = false
                                        viewModel.endTime = selectedTime.toLong()
                                    }) {
                                        Text("Potvrdit")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    if (viewModel.ulozit == 1) {
        Ulozeni()
        viewModel.ulozit = 0
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Treti() {
    val viewModel: CounterViewModel = viewModel()

    Box(
        modifier = Modifier
            .shadow(10.dp, RoundedCornerShape(36.dp))
            .fillMaxWidth(0.88f)
            .clip(RoundedCornerShape(36.dp))
            .background(Color(Bcolor))
            .height(210.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // ==== LEVÁ strana - radio buttons ====
            val radioOptions = listOf("Personal", "Family", "Boys")
            val selected = viewModel.selectedOption


            Column {
                radioOptions.forEach { text ->
                    Row(
                        Modifier
                            .height(40.dp)
                            .selectable(
                                selected = (text == selected),
                                onClick = { viewModel.selectedOption = text }
                            )
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (text == selected),
                            onClick = null
                        )
                        Text(
                            text = text,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }

            // ==== PRAVÁ strana - destination + notes ====

            Column(
                modifier = Modifier.width(175.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp), // mezera mezi boxy
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val suggestions = listOf("Doma", "Práce", "Škola", "Trénink")
                var expanded by remember { mutableStateOf(false) }
                var text by remember { mutableStateOf(viewModel.from) }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = {
                            text = it
                            viewModel.from = it
                            expanded = true // otevře nabídku při psaní
                        },
                        modifier = Modifier.fillMaxWidth()
                            .onFocusChanged { focusState ->
                            if (focusState.isFocused) expanded = true // otevře při kliknutí do textu
                        },
                        shape = RoundedCornerShape(30.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(Dcolor),
                            unfocusedBorderColor = Color(Ecolor),
                            cursorColor = Color(Dcolor),
                            focusedLabelColor = Color(Dcolor),
                            unfocusedTextColor = Color(Ccolor),
                            focusedTextColor = Color(Ccolor)
                        ),
                        label = { Text("From location") },
                        placeholder = { Text("Např. Doma") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )

                    val filteredSuggestions = suggestions.filter {
                        it.contains(text, ignoreCase = true)
                    }
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        filteredSuggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion) },
                                onClick = {
                                    text = suggestion
                                    viewModel.from = suggestion
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                val suggestions2 = listOf("Doma", "Práce", "Škola", "Trénink")
                var expanded2 by remember { mutableStateOf(false) }
                var text2 by remember { mutableStateOf(viewModel.destination) }

                ExposedDropdownMenuBox(
                    expanded = expanded2,
                    onExpandedChange = { expanded2 = !expanded2 }
                ) {
                    OutlinedTextField(
                        value = text2,
                        onValueChange = {
                            text2 = it
                            viewModel.destination = it
                            expanded2 = true // otevře nabídku při psaní
                        },
                        modifier = Modifier.fillMaxWidth()
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) expanded2 = true // otevře při kliknutí do textu
                            },
                        shape = RoundedCornerShape(30.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(Dcolor),
                            unfocusedBorderColor = Color(Ecolor),
                            cursorColor = Color(Dcolor),
                            focusedLabelColor = Color(Dcolor),
                            unfocusedTextColor = Color(Ccolor),
                            focusedTextColor = Color(Ccolor)
                        ),
                        label = { Text("Destination") },
                        placeholder = { Text("Např. Práce") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded2) }
                    )

                    val filteredSuggestions = suggestions2.filter {
                        it.contains(text2, ignoreCase = true)
                    }
                    ExposedDropdownMenu(
                        expanded = expanded2,
                        onDismissRequest = { expanded2 = false }
                    ) {
                        filteredSuggestions.forEach { suggestion ->
                            DropdownMenuItem(
                                text = { Text(suggestion) },
                                onClick = {
                                    text2 = suggestion
                                    viewModel.destination = suggestion
                                    expanded2 = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = viewModel.notes,
                    onValueChange = { viewModel.notes = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(Dcolor),
                        unfocusedBorderColor = Color(Ecolor),
                        cursorColor = Color(Dcolor),
                        focusedLabelColor = Color(Dcolor),
                        unfocusedTextColor = Color(Ccolor),
                        focusedTextColor = Color(Ccolor)
                    ),
                    label = { Text("Notes") },
                    placeholder = { Text("Něco dalšího?") }

                )
            }

        }
    }
}


fun formatInGroupsOfThreeFromEnd(input: String): String {
    val clean = input.replace(" ", "")
    val reversed = clean.reversed()
    val grouped = reversed.chunked(3).joinToString(" ")
    return grouped.reversed()
}


@Composable
fun Info() {
    val context = LocalContext.current
    val rideDao = remember { AppDatabase.getDatabase(context).rideDao() }
    val viewModel: CounterViewModel = viewModel()

    var rides by remember { mutableStateOf<List<Ride>>(emptyList()) }

    // Refresh při restartu
    if (viewModel.restarovat == 1) {
        LaunchedEffect(Unit) {
            rides = rideDao.getAllRides()
            viewModel.restarovat = 0
        }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {

        Text(
            "Seznam jízd",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Jízdy v opačném pořadí
        rides.asReversed().forEach { ride ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {

                    TableRow("Date", ride.date)
                    TableRow("Start Time", ride.startTime.let { formatTime(it) })
                    TableRow("Start km", ride.startKm ?: "-")
                    TableRow("Start fuel km", ride.startFuelKm ?: "-")
                    TableRow("End Time", ride.endTime?.let { formatTime(it) } ?: "-")
                    TableRow("End km", ride.endKm ?: "-")
                    TableRow("End fuel km", ride.endFuelKm ?: "-")
                    TableRow("Distance", "${ride.distance ?: "-"} km")
                    TableRow("Fuel", "${ride.fuel ?: "-"} L")
                    TableRow("Price", "${ride.price ?: "-"} Kč")
                    TableRow("Type",
                        if (ride.isPersonal == true) "Personal" else if (ride.isFamily == true) "Family" else if (ride.isBoys == true) "Boys" else "-"
                    )
                    TableRow("Avg speed", "${ride.averageSpeed ?: "-"} km/h")
                    TableRow("Ride time", "${ride.rideTime ?: "-"} s")
                    TableRow("Notes", ride.notes ?: "-")
                    TableRow("From", ride.from ?: "-")
                    TableRow("Destination", ride.destination ?: "-")
                    TableRow("Is finished", ride.isFinished.toString())
                    TableRow("Car", ride.car)
                    TableRow("ID", ride.id.toString())
                }
            }
        }
    }
}
@Composable
fun TableRow(label: String, value: Any?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(
            text = "$label:",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value?.toString() ?: "-",
            modifier = Modifier.weight(1f)
        )
    }
}



// Pomocná funkce pro formátování času
fun formatTime(millis: Long): String {
    val date = Date(millis)
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(date)
}

@Composable
fun Restart() {
    val viewModel: CounterViewModel = viewModel()
    Button(onClick = {viewModel.restarovat = 1},
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xffF1F2F6)))
    {Text("Znovu načíst všechny jízdy")}
}





