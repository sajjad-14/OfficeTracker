package com.example.officetracker.worker;

import android.content.Context;
import com.google.android.gms.location.GeofencingClient;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class GeofenceManager_Factory implements Factory<GeofenceManager> {
  private final Provider<Context> contextProvider;

  private final Provider<GeofencingClient> geofencingClientProvider;

  public GeofenceManager_Factory(Provider<Context> contextProvider,
      Provider<GeofencingClient> geofencingClientProvider) {
    this.contextProvider = contextProvider;
    this.geofencingClientProvider = geofencingClientProvider;
  }

  @Override
  public GeofenceManager get() {
    return newInstance(contextProvider.get(), geofencingClientProvider.get());
  }

  public static GeofenceManager_Factory create(Provider<Context> contextProvider,
      Provider<GeofencingClient> geofencingClientProvider) {
    return new GeofenceManager_Factory(contextProvider, geofencingClientProvider);
  }

  public static GeofenceManager newInstance(Context context, GeofencingClient geofencingClient) {
    return new GeofenceManager(context, geofencingClient);
  }
}
