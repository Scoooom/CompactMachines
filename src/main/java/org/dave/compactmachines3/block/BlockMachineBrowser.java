package org.dave.compactmachines3.block;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.dave.compactmachines3.CompactMachines3;
import org.dave.compactmachines3.network.MessageRequestMachineList;
import org.dave.compactmachines3.network.PackageHandler;
import org.dave.compactmachines3.reference.GuiIds;
import org.dave.compactmachines3.tile.TileEntityMachineBrowser;

public class BlockMachineBrowser extends BlockBase implements ITileEntityProvider {

    public BlockMachineBrowser(Material material) {
        super(material);
        this.setHardness(4.0F);
        this.setResistance(10.0F);
        this.setCreativeTab(CompactMachines3.CREATIVE_TAB);
    }

    @SideOnly(Side.CLIENT)
    public void initModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0,
                new ModelResourceLocation(getRegistryName(), "inventory"));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityMachineBrowser();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state,
            EntityPlayer player, EnumHand hand, EnumFacing side,
            float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            PackageHandler.instance.sendToServer(new MessageRequestMachineList(player.getUniqueID(), pos, world.provider.getDimension()));
            return true;
        }

        player.openGui(CompactMachines3.instance, GuiIds.MACHINE_BROWSER.ordinal(),
                world, pos.getX(), pos.getY(), pos.getZ());

        return true;
    }
}
