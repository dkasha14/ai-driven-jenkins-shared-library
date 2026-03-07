import unittest
from ai_engine.llm_client import ask_llm


class TestLLM(unittest.TestCase):

    def test_llm_response(self):
        response = ask_llm("Say hello")

        self.assertIsNotNone(response)
        self.assertTrue(len(response) > 0)


if __name__ == "__main__":
    unittest.main()
