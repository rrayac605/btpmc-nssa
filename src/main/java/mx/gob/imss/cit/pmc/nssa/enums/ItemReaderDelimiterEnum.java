package mx.gob.imss.cit.pmc.nssa.enums;

import org.springframework.batch.item.file.transform.Range;

import lombok.Getter;
import lombok.Setter;

public enum ItemReaderDelimiterEnum {

	RANGOS_NSSA(new Range[] { new Range(1, 3), new Range(4, 11), new Range(12, 13), new Range(14, 15),
			new Range(16, 18), new Range(19, 20), new Range(21, 22), new Range(23, 25), new Range(26, 28),
			new Range(29, 39), new Range(40, 50), new Range(51, 68), new Range(69, 118), new Range(119, 119),
			new Range(120, 120), new Range(121, 128), new Range(129, 136), new Range(137, 144), new Range(145, 152),
			new Range(153, 160), new Range(161, 168), new Range(169, 176), new Range(177, 179), new Range(180, 182),
			new Range(183, 189), new Range(190, 190), new Range(191, 194), new Range(195, 203), new Range(204, 212),
			new Range(213, 216), new Range(217, 220), new Range(221, 224), new Range(225, 228), new Range(229, 233),
			new Range(234, 234) }),

	RANGOS_NSSA_CARGA_INICIAL(new Range[] { new Range(1, 3), new Range(4, 11), new Range(12, 13), new Range(14, 15),
			new Range(16, 18), new Range(19, 20), new Range(21, 22), new Range(23, 25), new Range(26, 28),
			new Range(29, 39), new Range(40, 50), new Range(51, 68), new Range(69, 118), new Range(119, 119),
			new Range(120, 120), new Range(121, 128), new Range(129, 136), new Range(137, 144), new Range(145, 152),
			new Range(153, 160), new Range(161, 168), new Range(169, 176), new Range(177, 179), new Range(180, 182),
			new Range(183, 189), new Range(190, 190), new Range(191, 194), new Range(195, 203), new Range(204, 212),
			new Range(213, 216), new Range(217, 220), new Range(221, 224), new Range(225, 228), new Range(229, 233),
			new Range(234, 234), new Range(235, 304), new Range(305, 317), new Range(318, 319), new Range(320, 321), 
			new Range(322, 325), new Range(326, 329), new Range(330, 479), new Range(480, 486) });

	@Setter
	@Getter
	private Range[] valores;

	private ItemReaderDelimiterEnum(Range[] valores) {
		this.valores = valores;
	}

}
