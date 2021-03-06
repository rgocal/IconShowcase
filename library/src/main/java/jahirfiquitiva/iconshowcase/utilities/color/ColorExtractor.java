/*
 * Copyright (c) 2016. Jahir Fiquitiva. Android Developer. All rights reserved.
 */

package jahirfiquitiva.iconshowcase.utilities.color;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jahirfiquitiva.iconshowcase.R;
import jahirfiquitiva.iconshowcase.activities.ShowcaseActivity;
import jahirfiquitiva.iconshowcase.utilities.ThemeUtils;
import jahirfiquitiva.iconshowcase.utilities.Utils;


public class ColorExtractor {

    private static double previousOffset = -300;

    public static void setupToolbarIconsAndTextsColors(Context context, AppBarLayout appbar,
                                                       final Toolbar toolbar, final Bitmap bitmap,
                                                       boolean includeMutedSwatches) {

        final int iconsColor = ThemeUtils.darkTheme ?
                ContextCompat.getColor(context, R.color.toolbar_text_dark) :
                ContextCompat.getColor(context, R.color.toolbar_text_light);

        final int finalPaletteGeneratedColor = getFinalGeneratedIconsColorFromPalette(bitmap,
                context.getResources().getBoolean(R.bool.use_palette_api_in_toolbar),
                includeMutedSwatches);

        if (appbar != null) {
            appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                @SuppressWarnings("ResourceAsColor")
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    double alpha = round(((double) (verticalOffset * -1) / 288.0), 1);
                    int paletteColor = ColorUtils.blendColors(
                            finalPaletteGeneratedColor != 0 ? finalPaletteGeneratedColor : iconsColor,
                            iconsColor, alpha > 1.0 ? 1.0f : (float) alpha);
                    if (toolbar != null) {
                        ToolbarColorizer.colorizeToolbar(toolbar, paletteColor);
                        /*
                        // Collapsed offset = -352
                        if (verticalOffset != previousOffset) {
                            previousOffset = verticalOffset;
                        }
                        */
                    }
                }
            });
        }
    }

    public static int getIconsColorFromBitmap(Bitmap bitmap, boolean includeMutedSwatches) {
        int color = 0;
        if (bitmap != null) {
            Palette.Swatch swatch = getProminentSwatch(bitmap, includeMutedSwatches);
            if (swatch != null) {
                color = swatch.getBodyTextColor();
            }
        }
        return color;
    }

    public static int getFinalGeneratedIconsColorFromPalette(Bitmap bitmap, boolean usePalette, boolean includeMutedSwatches) {
        int generatedIconsColorFromPalette;
        if (usePalette) {
            generatedIconsColorFromPalette = getIconsColorFromBitmap(bitmap, includeMutedSwatches);
            if ((generatedIconsColorFromPalette == 0) && (bitmap != null)) {
                if (ColorUtils.isDark(bitmap)) {
                    generatedIconsColorFromPalette = Color.parseColor("#80ffffff");
                } else {
                    generatedIconsColorFromPalette = Color.parseColor("#66000000");
                }
            }
        } else {
            generatedIconsColorFromPalette = Color.parseColor("#99ffffff");
        }
        return generatedIconsColorFromPalette;
    }

    private static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static int getPreferredColor(Drawable drawable, Context context, boolean allowAccent, boolean includeMutedSwatches) {
        return getPreferredColor(Utils.drawableToBitmap(drawable), context, allowAccent, includeMutedSwatches);
    }

    public static int getPreferredColor(Bitmap bitmap, Context context, boolean allowAccent, boolean includeMutedSwatches) {
        Palette.Swatch prominentColor = getProminentSwatch(bitmap, includeMutedSwatches);
        int accent = ContextCompat.getColor(context, ThemeUtils.darkTheme ?
                R.color.dark_theme_accent : R.color.light_theme_accent);
        return prominentColor != null ? prominentColor.getRgb() : allowAccent ? accent : 0;
    }

    public static Palette.Swatch getProminentSwatch(Drawable drawable, boolean includeMutedSwatches) {
        return getProminentSwatch(Utils.drawableToBitmap(drawable), includeMutedSwatches);
    }

    public static Palette.Swatch getProminentSwatch(Bitmap bitmap, boolean includeMutedSwatches) {
        Palette palette = Palette.from(bitmap).generate();
        return getProminentSwatch(palette, includeMutedSwatches);
    }

    public static Palette.Swatch getProminentSwatch(Palette palette, boolean includeMutedSwatches) {
        if (palette == null) return null;
        List<Palette.Swatch> swatches = getSwatchesList(palette, includeMutedSwatches);
        return Collections.max(swatches,
                new Comparator<Palette.Swatch>() {
                    @Override
                    public int compare(Palette.Swatch opt1, Palette.Swatch opt2) {
                        int a = opt1 == null ? 0 : opt1.getPopulation();
                        int b = opt2 == null ? 0 : opt2.getPopulation();
                        return a - b;
                    }
                });
    }

    public static Palette.Swatch getLessProminentSwatch(Drawable drawable, boolean includeMutedSwatches) {
        return getLessProminentSwatch(Utils.drawableToBitmap(drawable), includeMutedSwatches);
    }

    public static Palette.Swatch getLessProminentSwatch(Bitmap bitmap, boolean includeMutedSwatches) {
        Palette palette = Palette.from(bitmap).generate();
        return getLessProminentSwatch(palette, includeMutedSwatches);
    }

    public static Palette.Swatch getLessProminentSwatch(Palette palette, boolean includeMutedSwatches) {
        if (palette == null) return null;
        List<Palette.Swatch> swatches = getSwatchesList(palette, includeMutedSwatches);
        return Collections.min(swatches,
                new Comparator<Palette.Swatch>() {
                    @Override
                    public int compare(Palette.Swatch opt1, Palette.Swatch opt2) {
                        int a = opt1 == null ? 0 : opt1.getPopulation();
                        int b = opt2 == null ? 0 : opt2.getPopulation();
                        return a - b;
                    }
                });
    }

    private static List<Palette.Swatch> getSwatchesList(Palette palette, boolean includeMutedSwatches) {
        List<Palette.Swatch> swatches = new ArrayList<>();

        Palette.Swatch vib = palette.getVibrantSwatch();
        Palette.Swatch vibLight = palette.getLightVibrantSwatch();
        Palette.Swatch vibDark = palette.getDarkVibrantSwatch();

        swatches.add(vib);
        swatches.add(vibLight);
        swatches.add(vibDark);

        if (includeMutedSwatches) {
            addMutedSwatchesToSwatchesList(palette, swatches);
        }

        return swatches;
    }

    private static void addMutedSwatchesToSwatchesList(Palette palette, List<Palette.Swatch> swatches) {
        Palette.Swatch muted = palette.getMutedSwatch();
        Palette.Swatch mutedLight = palette.getLightMutedSwatch();
        Palette.Swatch mutedDark = palette.getDarkMutedSwatch();
        swatches.add(muted);
        swatches.add(mutedLight);
        swatches.add(mutedDark);
    }

}