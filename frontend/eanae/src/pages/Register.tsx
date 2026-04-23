import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useRegister } from "../hooks/useResister";
import { useForm } from "react-hook-form";
import Card from "../components/common/Card";

const schema = z.object({
    name: z.string().min(1, "Name is required"),
    email: z.string("Invalid email format").email(),
    password: z
        .string()
        .min(8, "Password must be at least 8 characters")
        .regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$$!%*?&]).{8}$/, "Must contain upper, lower, number & special char"),
    phone: z.string().regex(/^\+?[1-9]\d{1,14}$/, "Invalid phone number format"),
});

type FormData = z.infer<typeof schema>;

export default function Register() {
    const { mutate, isPending } = useRegister();
    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<FormData>({
        resolver: zodResolver(schema),
    });

    const onSubmit = (data: FormData) => {
        mutate(data, {
            onSuccess: () => {
                alert("Registered successfully");
            },
            onError: (err: any) => {
                const msg =
                    err?.response?.data?.message ||
                    err?.response?.data?.errors?.[0] ||
                    "Registration failed";

                alert(msg);
            },
        });
    };

    return (
        <div className="flex justify-center items-center min-h-screen">
            <Card>
                <form onSubmit={handleSubmit(onSubmit)} className="flex flex-col gap-4 w-80">
                    <h2 className="text-lg font-semibold">Register</h2>
                    <div>
                        <label htmlFor="name">Name:</label>
                        <input
                            type="text"
                            id="name"
                            {...register("name")}
                            className="w-full p-2 border rounded focus:outline-blue-500"
                            placeholder="Enter your name"
                        />
                        {errors.name && <span className="text-red-500">{errors.name.message}</span>}
                    </div>
                    <div>
                        <label htmlFor="email">Email:</label>
                        <input
                            type="email"
                            id="email"
                            {...register("email")}
                            className="w-full p-2 border rounded focus:outline-blue-500"
                            placeholder="Enter your email"
                        />
                        {errors.email && <span className="text-red-500">{errors.email.message}</span>}
                    </div>
                    <div>
                        <label htmlFor="password">Password:</label>
                        <input
                            type="password"
                            id="password"
                            {...register("password")}
                            className="w-full p-2 border rounded focus:outline-blue-500"
                            placeholder="Enter your password"
                        />
                        {errors.password && <span className="text-red-500">{errors.password.message}</span>}
                    </div>
                    <div>
                        <label htmlFor="phone">Phone:</label>
                        <input
                            type="tel"
                            id="phone"
                            {...register("phone")}
                            className="w-full p-2 border rounded focus:outline-blue-500"
                            placeholder="Enter your phone number"
                        />
                        {errors.phone && <span className="text-red-500">{errors.phone.message}</span>}
                    </div>
                    <button type="submit" className="bg-blue-500 text-white p-2 rounded hover:bg-blue-600">
                        Register
                    </button>
                </form>
            </Card>
        </div>
    );
}