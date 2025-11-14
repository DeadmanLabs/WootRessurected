package ipsis.woot.gui;

import ipsis.woot.Woot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registration for Woot menu types
 */
public class WootMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
        DeferredRegister.create(Registries.MENU, Woot.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<FactoryHeartMenu>> FACTORY_HEART =
        MENU_TYPES.register("factory_heart",
            () -> IMenuTypeExtension.create((containerId, inventory, data) -> {
                BlockPos pos = data.readBlockPos();
                return new FactoryHeartMenu(containerId, inventory, pos);
            })
        );
}
