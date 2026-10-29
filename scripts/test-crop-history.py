import importlib.util, unittest
spec=importlib.util.spec_from_file_location('crop','scripts/prepare-crop-history.py'); m=importlib.util.module_from_spec(spec); spec.loader.exec_module(m)
class CropHistoryMigrationTest(unittest.TestCase):
    def test_preservation_and_missing(self):
        codes=['PM04D','PM05E','PM06F','PM08G','PM10K','PM11M','PM15P']
        base={'estateId':'selendang-3','revision':4,'exportedAt':'old','unknown':{'keep':42},'sections':[{'id':'fields','columns':['Field code','Area (ha)'],'rows':[[c,10] for c in codes[:-1]]}]}
        source='## Crop History\n'+'\n'.join('| '+c+' | N/A | 1 | 2 | 3 | 4 |' for c in codes)+'\n---\n## Selendang 3 - Crop Production (Total Tonnes) for 2026\n'+'\n'.join('| '+c+' | 10 | 20 | 30 | 40 | 50 | 60 | 70 | 80 | 90 | | | | 450 |' for c in codes)+'\n---'
        out=m.prepare(base,source)
        self.assertEqual(out['revision'],5)
        self.assertEqual(out['unknown'],base['unknown'])
        self.assertEqual(out['sections'],base['sections'])
        self.assertNotIn('cropHistory',base)
        self.assertIsNone(out['cropHistory']['fields'][-1]['plantedAreaHa'])
        self.assertIsNone(out['cropHistory']['fields'][0]['annual']['2021'])
        self.assertEqual(out['cropHistory']['cutoff'],'2026-09-30')
        with self.assertRaises(AssertionError): m.prepare(base,source.replace('450','451'))
        with self.assertRaises(AssertionError): m.prepare(base,source.replace('| 40 |','| |'))
unittest.main()
