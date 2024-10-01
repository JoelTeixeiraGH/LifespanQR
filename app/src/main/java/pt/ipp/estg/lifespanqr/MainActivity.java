package pt.ipp.estg.lifespanqr;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import pt.ipp.estg.lifespanqr.authentication.ProfileDialogFragment;
import pt.ipp.estg.lifespanqr.camera.Camera;
import pt.ipp.estg.lifespanqr.camera.CameraFragment;
import pt.ipp.estg.lifespanqr.database.IngredientRepository;
import pt.ipp.estg.lifespanqr.ingredient.IngredientListFragment;
import pt.ipp.estg.lifespanqr.ingredient.IngredientListViewModel;
import static pt.ipp.estg.lifespanqr.ingredient.IngredientAdapter.TAG.CHECKED;
import static pt.ipp.estg.lifespanqr.ingredient.IngredientAdapter.TAG.UNCHECKED;

public class MainActivity extends AppCompatActivity{

    private static final int RC_SIGN_IN = 4001;
    private static final int RC_WRITE_CALENDAR = 4002;
    private static final String TAG = "MainActivity";
    private static boolean hasCalendarPermissions;
    private IngredientListFragment mainListFragment, historyFragment;
    private CameraFragment cameraFragment;
    private TabLayout tabs;
    private Toolbar toolbar;
    private GoogleSignInClient mGoogleSignInClient;
    private Bitmap profilePic;
    private Menu menu;
    private MainActivityViewModel viewModel;
    private TabLayout.OnTabSelectedListener tabListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel.class);
        viewModel.getProfilePic().observe(this,
                profilePic -> this.profilePic = profilePic);

        tabListener = new TabLayout.OnTabSelectedListener(){
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch(tab.getPosition()){
                    case 2:
                        replaceFragment(historyFragment);
                        break;
                    case 1:
                        tab.setIcon(null);
                        tab.setText(R.string.action_capture);
                        replaceFragment(cameraFragment);
                        break;
                    default:
                        replaceFragment(mainListFragment);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1){
                    tab.setText("");
                    tab.setIcon(R.drawable.ic_baseline_camera_alt_24);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1){
                    Camera.takePicture();
                }
            }
        };

        setToolbar();
        setTabs();
        setFragments();
        replaceFragment(mainListFragment);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestProfile()
                .requestEmail()
                .build();

        requestCalendarPermissions();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        try {
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch(Exception e) {
            Log.e(TAG, e.toString());
        }
    }

    private void setFragments() {
        cameraFragment = new CameraFragment(R.layout.camera_fragment);
        mainListFragment = new IngredientListFragment(R.layout.list_fragment, UNCHECKED);
        historyFragment = new IngredientListFragment(R.layout.list_fragment, CHECKED);
    }

    private void setTabs() {
        tabs = findViewById(R.id.tabs);
        tabs.addOnTabSelectedListener(tabListener);
    }

    private void setToolbar() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        this.menu = menu;
        setUserChange();
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        switch(item.getItemId()){
            case R.id.menu_account:
                if(currentUser == null) {
                    startSignIn();
                } else {
                    ProfileDialogFragment dialog =
                            new ProfileDialogFragment(currentUser, profilePic, this::signOut);
                    dialog.show(getSupportFragmentManager(), null);
                }
                return true;
            case R.id.action_delete_all:
                AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                        .setMessage("Do you want to delete all your ingredients?")
                        .setPositiveButton("Delete", (dialog1, which) ->
                                ViewModelProviders.of(MainActivity.this)
                                        .get(IngredientListViewModel.class)
                                        .deleteAll())
                        .setNegativeButton("Cancel", (dialog12, which) -> dialog12.dismiss())
                        .create();
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.show();
                return true;
        }
        return false;
    }

    public void replaceFragment(Fragment fragment){
        if (fragment.equals(cameraFragment)) toolbar.setTitle("QR Code Scanner");
        if (fragment.equals(historyFragment)) toolbar.setTitle("Consumed/Expired");
        if (fragment.equals(mainListFragment)) toolbar.setTitle("Ingredients");

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void signOut(){
        mGoogleSignInClient.signOut().addOnCompleteListener(task -> {
            Toast.makeText(MainActivity.this, "Signed out", Toast.LENGTH_SHORT).show();
            IngredientRepository.removeFirebaseChildEventListener();
            FirebaseAuth.getInstance().signOut();
            setUserChange();
        });
    }

    public void startSignIn(){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // No-op
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String username =
                                FirebaseAuth.getInstance().getCurrentUser().getDisplayName()
                                        .split(" ")[0];

                        Toast.makeText(this, "Welcome " + username, Toast.LENGTH_SHORT)
                                .show();
                        setUserChange();
                    } else {
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT)
                                .show();
                        setUserChange();
                    }
                });
    }

    private void setUserChange() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        updateUserIcon(currentUser);

        if(currentUser != null){
            IngredientRepository.useFirebase();
        } else {
            IngredientRepository.useRoom();
        }
    }

    private void updateUserIcon(FirebaseUser user){
        if(user!=null) {
              new GetProfilePicAsyncTask(menu, user, this::setGoogleProfilePic).execute();
        } else {
            menu.getItem(1).setIcon(ContextCompat.getDrawable(this, R.drawable.ic_baseline_account_circle_24));
        }
    }

    private void setGoogleProfilePic(Bitmap bitmap){
        // https://stackoverflow.com/questions/5882180/how-to-set-bitmap-in-circular-imageview
        Bitmap circleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        BitmapShader shader = new BitmapShader (bitmap,  Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        Paint paint = new Paint();
        paint.setShader(shader);
        paint.setAntiAlias(true);
        Canvas c = new Canvas(circleBitmap);
        c.drawCircle(bitmap.getWidth()/2, bitmap.getHeight()/2, bitmap.getWidth()/2, paint);

        profilePic = circleBitmap;
        viewModel.setProfilePic(profilePic);
        BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), circleBitmap);
        menu.getItem(1).setIcon(bitmapDrawable);
    };

    private void requestCalendarPermissions(){
        if(!checkSelfPermissions()){
            requestPermissions(new String[]{
                    Manifest.permission.WRITE_CALENDAR,
                    Manifest.permission.READ_CALENDAR
            }, RC_WRITE_CALENDAR);
        } else {
            hasCalendarPermissions = true;
            IngredientCalendar.createInstance(this);
        }
    }

    private boolean checkSelfPermissions(){
        return ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_CALENDAR)
                == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == RC_WRITE_CALENDAR){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                hasCalendarPermissions = true;
                IngredientCalendar.createInstance(this);
            } else {
                hasCalendarPermissions = false;
                Toast.makeText(this, "Expiry reminders disabled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static boolean hasCalendarPermissions(){
        return hasCalendarPermissions;
    }
}
