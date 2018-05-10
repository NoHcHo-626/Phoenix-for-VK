package biz.dealnote.messenger.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collection;

/**
 * Created by admin on 04.12.2016.
 * phoenix
 */
public interface IOwnersBundle {

    @Nullable
    Owner findById(int id);

    @NonNull
    Owner getById(int id);

    int size();

    void putAll(@NonNull Collection<? extends Owner> owners);

    void put(@NonNull Owner owner);

    Collection<Integer> getMissing(Collection<Integer> ids);
}