/*
 * Copyright (C) 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.net;

import android.annotation.IntDef;
import android.annotation.NonNull;
import android.os.Bundle;
import android.os.Parcelable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** @hide */
public final class OemNetworkPreferences implements Parcelable {
    /**
     * Use default behavior requesting networks. Equivalent to not setting any preference at all.
     */
    public static final int OEM_NETWORK_PREFERENCE_DEFAULT = 0;

    /**
     * If an unmetered network is available, use it.
     * Otherwise, if a network with the OEM_PAID capability is available, use it.
     * Otherwise, use the general default network.
     */
    public static final int OEM_NETWORK_PREFERENCE_OEM_PAID = 1;

    /**
     * If an unmetered network is available, use it.
     * Otherwise, if a network with the OEM_PAID capability is available, use it.
     * Otherwise, the app doesn't get a network.
     */
    public static final int OEM_NETWORK_PREFERENCE_OEM_PAID_NO_FALLBACK = 2;

    /**
     * Prefer only NET_CAPABILITY_OEM_PAID networks.
     */
    public static final int OEM_NETWORK_PREFERENCE_OEM_PAID_ONLY = 3;

    /**
     * Prefer only NET_CAPABILITY_OEM_PRIVATE networks.
     */
    public static final int OEM_NETWORK_PREFERENCE_OEM_PRIVATE_ONLY = 4;

    @NonNull
    private final Bundle mNetworkMappings;

    @NonNull
    public Map<String, Integer> getNetworkPreferences() {
        return convertToUnmodifiableMap(mNetworkMappings);
    }

    private OemNetworkPreferences(@NonNull final Bundle networkMappings) {
        Objects.requireNonNull(networkMappings);
        mNetworkMappings = (Bundle) networkMappings.clone();
    }

    @Override
    public String toString() {
        return "OemNetworkPreferences{" + "mNetworkMappings=" + mNetworkMappings + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OemNetworkPreferences that = (OemNetworkPreferences) o;

        return mNetworkMappings.size() == that.mNetworkMappings.size()
                && mNetworkMappings.toString().equals(that.mNetworkMappings.toString());
    }

    @Override
    public int hashCode() {
        return Objects.hash(mNetworkMappings);
    }

    /**
     * Builder used to create {@link OemNetworkPreferences} objects.  Specify the preferred Network
     * to package name mappings.
     *
     * @hide
     */
    public static final class Builder {
        private final Bundle mNetworkMappings;

        public Builder() {
            mNetworkMappings = new Bundle();
        }

        public Builder(@NonNull final OemNetworkPreferences preferences) {
            Objects.requireNonNull(preferences);
            mNetworkMappings = (Bundle) preferences.mNetworkMappings.clone();
        }

        /**
         * Add a network preference for a given package. Previously stored values for the given
         * package will be overwritten.
         *
         * @param packageName full package name (e.g.: "com.google.apps.contacts") of the app
         *                    to use the given preference
         * @param preference  the desired network preference to use
         * @return The builder to facilitate chaining.
         */
        @NonNull
        public Builder addNetworkPreference(@NonNull final String packageName,
                @OemNetworkPreference final int preference) {
            Objects.requireNonNull(packageName);
            mNetworkMappings.putInt(packageName, preference);
            return this;
        }

        /**
         * Remove a network preference for a given package.
         *
         * @param packageName full package name (e.g.: "com.google.apps.contacts") of the app to
         *                    remove a preference for.
         * @return The builder to facilitate chaining.
         */
        @NonNull
        public Builder removeNetworkPreference(@NonNull final String packageName) {
            Objects.requireNonNull(packageName);
            mNetworkMappings.remove(packageName);
            return this;
        }

        /**
         * Build {@link OemNetworkPreferences} return the current OEM network preferences.
         */
        @NonNull
        public OemNetworkPreferences build() {
            return new OemNetworkPreferences(mNetworkMappings);
        }
    }

    private static Map<String, Integer> convertToUnmodifiableMap(@NonNull final Bundle bundle) {
        final Map<String, Integer> networkPreferences = new HashMap<>();
        for (final String key : bundle.keySet()) {
            networkPreferences.put(key, bundle.getInt(key));
        }
        return Collections.unmodifiableMap(networkPreferences);
    }

    /** @hide */
    @IntDef(prefix = "OEM_NETWORK_PREFERENCE_", value = {
            OEM_NETWORK_PREFERENCE_DEFAULT,
            OEM_NETWORK_PREFERENCE_OEM_PAID,
            OEM_NETWORK_PREFERENCE_OEM_PAID_NO_FALLBACK,
            OEM_NETWORK_PREFERENCE_OEM_PAID_ONLY,
            OEM_NETWORK_PREFERENCE_OEM_PRIVATE_ONLY
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface OemNetworkPreference {}

    /**
     * Return the string value for OemNetworkPreference
     *
     * @param value int value of OemNetworkPreference
     * @return string version of OemNetworkPreference
     */
    @NonNull
    public static String oemNetworkPreferenceToString(@OemNetworkPreference int value) {
        switch (value) {
            case OEM_NETWORK_PREFERENCE_DEFAULT:
                return "OEM_NETWORK_PREFERENCE_DEFAULT";
            case OEM_NETWORK_PREFERENCE_OEM_PAID:
                return "OEM_NETWORK_PREFERENCE_OEM_PAID";
            case OEM_NETWORK_PREFERENCE_OEM_PAID_NO_FALLBACK:
                return "OEM_NETWORK_PREFERENCE_OEM_PAID_NO_FALLBACK";
            case OEM_NETWORK_PREFERENCE_OEM_PAID_ONLY:
                return "OEM_NETWORK_PREFERENCE_OEM_PAID_ONLY";
            case OEM_NETWORK_PREFERENCE_OEM_PRIVATE_ONLY:
                return "OEM_NETWORK_PREFERENCE_OEM_PRIVATE_ONLY";
            default:
                return Integer.toHexString(value);
        }
    }

    @Override
    public void writeToParcel(@NonNull android.os.Parcel dest, int flags) {
        dest.writeBundle(mNetworkMappings);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public static final Parcelable.Creator<OemNetworkPreferences> CREATOR =
            new Parcelable.Creator<OemNetworkPreferences>() {
                @Override
                public OemNetworkPreferences[] newArray(int size) {
                    return new OemNetworkPreferences[size];
                }

                @Override
                public OemNetworkPreferences createFromParcel(@NonNull android.os.Parcel in) {
                    return new OemNetworkPreferences(
                            in.readBundle(getClass().getClassLoader()));
                }
            };
}
