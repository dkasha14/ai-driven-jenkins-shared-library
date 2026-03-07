import os
from groq import Groq
from dotenv import load_dotenv

load_dotenv()

def get_llm_client():

    api_key = os.getenv("GROQ_API_KEY")

    if not api_key:
        raise ValueError("GROQ_API_KEY not found in environment variables")

    client = Groq(api_key=api_key)

    return client


def ask_llm(prompt):

    client = get_llm_client()

    response = client.chat.completions.create(

        messages=[
            {
                "role": "user",
                "content": prompt
            }
        ],

        model="llama-3.3-70b-versatile"
    )

    return response.choices[0].message.content