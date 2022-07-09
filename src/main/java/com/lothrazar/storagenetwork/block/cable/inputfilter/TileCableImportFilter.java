package com.lothrazar.storagenetwork.block.cable.inputfilter;

import com.lothrazar.storagenetwork.api.EnumConnectType;
import com.lothrazar.storagenetwork.api.EnumStorageDirection;
import com.lothrazar.storagenetwork.block.TileCableWithFacing;
import com.lothrazar.storagenetwork.block.cable.BlockCable;
import com.lothrazar.storagenetwork.capability.CapabilityConnectableAutoIO;
import com.lothrazar.storagenetwork.registry.SsnRegistry;
import com.lothrazar.storagenetwork.registry.StorageNetworkCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

public class TileCableImportFilter extends TileCableWithFacing implements MenuProvider {

  protected CapabilityConnectableAutoIO ioStorage;

  public TileCableImportFilter(BlockPos pos, BlockState state) {
    super(SsnRegistry.FILTERIMPORTKABELTILE, pos, state);
    this.ioStorage = new CapabilityConnectableAutoIO(this, EnumStorageDirection.IN);
  }

  @Override
  public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity) {
    return new ContainerCableImportFilter(i, level, worldPosition, playerInventory, playerEntity);
  }

  @Override
  public Component getDisplayName() {
    return new TranslatableComponent(getType().getRegistryName().getPath());
  }

  @Override
  public void setDirection(Direction direction) {
    super.setDirection(direction);
    this.ioStorage.setInventoryFace(direction);
  }

  @Override
  public void load(CompoundTag compound) {
    this.ioStorage.deserializeNBT(compound.getCompound("ioStorage"));
    ioStorage.upgrades.deserializeNBT(compound.getCompound("upgrades"));
    super.load(compound);
  }

  @Override
  public void saveAdditional(CompoundTag compound) {
    super.saveAdditional(compound);
    compound.put("ioStorage", this.ioStorage.serializeNBT());
    compound.put("upgrades", ioStorage.upgrades.serializeNBT());
  }

  @Override
  public <T> LazyOptional<T> getCapability(Capability<T> capability, Direction facing) {
    if (capability == StorageNetworkCapabilities.CONNECTABLE_AUTO_IO) {
      LazyOptional<CapabilityConnectableAutoIO> cap = LazyOptional.of(() -> ioStorage);
      return cap.cast();
    }
    //    if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) { 
    //      LazyOptional<IItemHandler> cap = LazyOptional.of(() -> ioStorage.upgrades);
    //      return cap.cast();
    //    }
    return super.getCapability(capability, facing);
  }

  private void tick() {
    if (this.getDirection() == null) {
      this.findNewDirection();
      if (getDirection() != null) {
        BlockState newState = BlockCable.cleanBlockState(this.getBlockState());
        newState = newState.setValue(BlockCable.FACING_TO_PROPERTY_MAP.get(getDirection()), EnumConnectType.CABLE);
        level.setBlockAndUpdate(worldPosition, newState);
      }
    }
  }

  public static void clientTick(Level level, BlockPos blockPos, BlockState blockState, TileCableImportFilter tile) {
    tile.tick();
  }

  public static <E extends BlockEntity> void serverTick(Level level, BlockPos blockPos, BlockState blockState, TileCableImportFilter tile) {
    tile.tick();
  }
}
