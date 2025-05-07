package me.yonezu;

import com.google.gson.JsonObject;
import me.yonezu.annotations.PeriodicMethod;
import me.yonezu.ws.WSClient;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author JellyLabs
 * @version 1.0
 * @since 1.0
 */
public class JellyLabApi {
    public String modName;
    public String modVersion;
    public String uuid;
    public String instanceID;
    private static JellyLabApi INSTANCE;

    public static JellyLabApi getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JellyLabApi();
        }
        return INSTANCE;
    }

    /**
     * Start the analytics
     *
     * @param modName    The name of the mod
     * @param modVersion The version of the mod
     * @param uuid       The uuid of the player
     */
    public void startAnalytics(String modName, String modVersion, String uuid, String instanceID) {
        this.modName = modName;
        this.modVersion = modVersion;
        this.uuid = uuid;
        this.instanceID = instanceID;
        System.out.println("[JellyLabAPI] " + modName + " " + modVersion + " started analytics with uuid " + uuid);
        try {
            WSClient client = new WSClient(new URI("ws://localhost:38256"));
            client.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PeriodicMethod(name = "sendPeriodicData", time = 60)
    public JsonObject sendPeriodicData() {
        startAnalytics(modName, modVersion, uuid, instanceID);
        // some random data
        // Data interestingDate = Utils.getInterestingData();
        return new JsonObject();
    }

    public void register(Object object) {
        Class<?> clazz = object.getClass();
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            PeriodicMethod annotation = method.getAnnotation(PeriodicMethod.class);
            if (annotation != null) {
                String commandName = annotation.name();
                int timeInSeconds = annotation.time();

                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            Object result = method.invoke(object);
                            // Use the data received from mod
                            System.out.println("Received result: " + result);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                };
                Timer timer = new Timer();
                timer.schedule(task, 0, timeInSeconds * 1000L);
                System.out.println("Registered periodic method: " + method.getName() + ", command name: " + commandName + ", time: " + timeInSeconds + " seconds");
            }
        }
    }
}