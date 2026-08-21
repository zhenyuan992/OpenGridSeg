# OpenGridSeg

[![Tests](https://github.com/zhenyuan992/OpenGridSeg/actions/workflows/test.yml/badge.svg)](https://github.com/zhenyuan992/OpenGridSeg/actions/workflows/test.yml)
[![Release](https://img.shields.io/github/v/release/zhenyuan992/OpenGridSeg)](https://github.com/zhenyuan992/OpenGridSeg/releases/latest)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**OpenGridSeg** means **Open Grid Segmentation**. It is a free, open-source Fiji plugin for finding repeated bar grids, reviewing GFP and mCherry signal, and exporting horizontal multichannel time-series crops.

## Features

- Detects complete and partial 20×20 bar arrays.
- Supports more than one physical array in a field of view.
- Uses a selected bright-field frame for detection.
- Suggests bars to keep from GFP and mCherry signal over time.
- Lets users change one bar or a 5×5 block by hand.
- Shows aligned BF, GFP, and mCherry previews and full-field maps.
- Exports OME-TIFF with one `XYCT` float32 series per selected bar.
- Keeps source digital-number values. Display scaling never changes exported pixels.
- Saves CSV measurements and JSON settings for traceability.

## Requirements

- A recent [Fiji](https://fiji.sc/) installation.
- No Python installation is needed.
- BF, GFP, and mCherry images must already be spatially aligned.

## Install

1. Download `OpenGridSeg.jar` from the [latest release](https://github.com/zhenyuan992/OpenGridSeg/releases/latest).
2. Close Fiji.
3. Remove older copies of this plugin from Fiji's `plugins` folder.
4. Copy `OpenGridSeg.jar` into the `plugins` folder.
5. Restart Fiji.
6. Open `Plugins > OpenGridSeg`.

## File names

Put all TIFF planes directly in one folder. OpenGridSeg does not search inside subfolders.

Use names like:

```text
experiment_A_w1GFP_t1.TIF
experiment_A_w2mCherry_t1.TIF
experiment_A_w3BF_t1.TIF
```

OpenGridSeg reads:

- `w1` as GFP;
- `w2` as mCherry;
- `w3` as bright field (BF);
- `t1`, `t2`, and so on as time points;
- everything before `_w1`, `_w2`, or `_w3` as the field-of-view name.

Keep the field-of-view text identical for all three channels. Time points must start at `t1` and have no gaps. Do not put another underscore between the `w1`/`w2`/`w3` channel text and `_tN`.

`.nd` files are ignored. All three channels must contain the same time points and image size.

## Use

1. Open `Plugins > OpenGridSeg` and choose the TIFF folder.
2. Pick a field of view and BF frame. Frame 1 is the default.
3. Set the approximate **Template bar (length / width, px)**.
4. Click **1. Detect / Update Preview**.
5. Check the rotated preview. Bars should be horizontal.
6. Uncheck a wrong array or correct its starting row and column.
7. Set the export crop. The default is 60×30 pixels.
8. Click **2. Review GFP / mCherry Bars**.
9. Check or uncheck bars. `GFP OR mCherry` is the default rule.
10. Click **Use Selection**.
11. Click **3. Export Selected Bars**.

**Preview with per-channel scaling** starts on. It makes each preview easier to see. It changes display only.

The full-field map uses green circles for kept bars and red X marks for rejected bars. **Toggle in a 5×5 selection** changes a block without crossing into another physical array.

## Automatic first pass

OpenGridSeg compares each bar with its nearby background over time. A channel passes when at least two of these three checks pass:

- repeated brightness score `Z90 ≥ 5`;
- repeated bright area `A90 ≥ 12 pixels`;
- signal persistence `≥ 20%` of frames.

This is only a first suggestion. Users can change every choice before export.

## Output

For each field of view, OpenGridSeg writes:

- `*_bar_crops.ome.tif`: one horizontal `XYCT` float32 OME series per selected bar;
- `*_bars.csv`: coordinates, source files, scores, decisions, and OME series mapping;
- `*_run_config.json`: settings, provenance, selection rule, and manual changes.

Exports stay in the source digital-number scale. OpenGridSeg never normalizes or stretches exported values. Bicubic interpolation is the default. Bilinear and nearest-neighbour are also available.

## Build and test

Development uses JDK 21 and Maven. The built plugin targets Java 8 bytecode for Fiji compatibility.

```bash
mvn clean test package
```

To build, install, and ask Fiji to verify the menu entry:

```bash
FIJI_DIR=/absolute/path/to/Fiji bash scripts/001_build_install_verify.sh
```

## Citation

Citation metadata are provided in [`CITATION.cff`](CITATION.cff). GitHub also shows a **Cite this repository** button.

## Contributing

Bug reports and pull requests are welcome. See [`CONTRIBUTING.md`](CONTRIBUTING.md).

## License

OpenGridSeg is released under the [MIT License](LICENSE). You may use, copy, change, and redistribute it, including for commercial work, under the license terms.
